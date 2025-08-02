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
import java.util.*;

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

            Set<String> existingIsins = new HashSet<>(stockRepository.findAllIsins());

            if(!existingIsins.isEmpty()) return RepeatStatus.FINISHED;

            List<Stock> toSave = new ArrayList<>();
            for (int page = 1; page <= 10; page++) {
                URI uri = URI.create(apiUrl
                        + "?serviceKey=" + serviceKey
                        + "&resultType=json"
                        + "&numOfRows=" + pageSize
                        + "&pageNo=" + page);

                String json = rt.getForObject(uri, String.class);

                if (json == null) continue;

                JsonNode items = mapper.readTree(json)
                        .path("response")
                        .path("body")
                        .path("items")
                        .path("item");

                if (items.isArray()) {
                    for (JsonNode item : items) {
                        String symbol = item.path("srtnCd").asText();
                        // 이미 존재하는 심볼이면 건너뛰기
                        if (existingIsins.contains(symbol)) continue;
                        existingIsins.add(symbol);

                        toSave.add(Stock.builder()
                                .symbol(symbol)
                                .name(item.path("itmsNm").asText())
                                .market(item.path("mrktCtg").asText())
                                .isin(item.path("isinCd").asText())
                                .build());
                    }
                }
                // 페이지 단위로 배치 저장 (원하는 빈도로)
                if (!toSave.isEmpty()) {
                    stockRepository.saveAll(toSave);
                    toSave.clear();
                }
            }

            return RepeatStatus.FINISHED;
        };
    }
}