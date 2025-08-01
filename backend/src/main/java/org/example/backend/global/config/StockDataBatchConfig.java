package org.example.backend.global.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.domain.stock.repository.StockRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class StockDataBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final StockRepository stockRepository;
    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${openapi.apiUrl}")
    private String apiUrl;

    @Value("${openapi.serviceKey}")
    private String serviceKey;

    @Value("${openapi.page-size}")
    private int pageSize;

    @Bean
    public RestTemplate restTemplate() {
        return restTemplateBuilder.build();
    }

    @Bean
    public Job importStockJob() {
        return new JobBuilder("importStockJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(importStockStep())
                .build();
    }

    @Bean
    public Step importStockStep() {
        return new StepBuilder("importStockStep", jobRepository)
                .tasklet(stockTasklet(), transactionManager)
                .build();
    }

    /**
     * 실제 작업: 금융위원회_주식시세정보 API 호출 → JSON 파싱 → Stock 저장
     */
    @Bean
    public Tasklet stockTasklet() {
        return (contribution, chunkContext) -> {
            RestTemplate rt = restTemplate();
            ObjectMapper mapper = new ObjectMapper();

            // 1) 첫 호출: 전체 건수(totalCount) 조회
            URI firstUri = URI.create(apiUrl
                    + "?serviceKey=" + serviceKey
                    + "&resultType=json"
                    + "&numOfRows=" + pageSize
                    + "&pageNo=1");
            String firstJson = rt.getForObject(firstUri, String.class);
            JsonNode body = mapper.readTree(firstJson)
                    .path("response")
                    .path("body");
            int totalCount = body.path("totalCount").asInt();
            int totalPage = (totalCount + pageSize - 1) / pageSize;

            // 2) 페이지마다 반복 호출 -> 기존 데이터 수는 300만개, 그러나 시간이 너무 오래걸림 -> 1만개로 축소
            // 나중에 300만개 모두 받으려면 10 -> totalPage로 리펙토링
            for (int page = 1; page <= 10; page++) {
                URI uri = URI.create(apiUrl
                        + "?serviceKey=" + serviceKey
                        + "&resultType=json"
                        + "&numOfRows=" + pageSize
                        + "&pageNo=" + page);

                String json = rt.getForObject(uri, String.class);
                if (json == null) {
                    continue;
                }

                JsonNode items = mapper.readTree(json)
                        .path("response")
                        .path("body")
                        .path("items")
                        .path("item");

                if (items.isArray()) {
                    for (JsonNode item : items) {
                        String symbol = item.path("srtnCd").asText();
                        // 이미 존재하는 심볼이면 건너뛰기
                        Optional<Stock> existing = stockRepository.findBySymbol(symbol);
                        if (existing.isPresent()) {
                            continue;
                        }

                        Stock stock = Stock.builder()
                                .symbol(item.path("srtnCd").asText())
                                .name(item.path("itmsNm").asText())
                                .market(item.path("mrktCtg").asText())
                                .isin(item.path("isinCd").asText())
                                .build();
                        stockRepository.save(stock);
                    }
                }
            }

            return RepeatStatus.FINISHED;
        };
    }
}