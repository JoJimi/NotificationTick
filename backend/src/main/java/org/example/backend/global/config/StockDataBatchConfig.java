package org.example.backend.global.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.domain.stock.repository.StockRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openapi.apiUrl}")
    private String apiUrl;

    @Value("${openapi.serviceKey}")
    private String serviceKey;

    @Value("${openapi.page-size}")
    private int pageSize;

    private static final int MAX_PAGE = 10;      // 전체 페이지 수
    private static final int GRID_SIZE = 5;      // 파티션 개수
    private Set<String> existingSymbols = new HashSet<>();

    @Bean
    public RestTemplate restTemplate() {
        return restTemplateBuilder.build();
    }

    @Bean
    public Job importStockJob() {
        return new JobBuilder("importStockJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(masterStep())
                .build();
    }

    @Bean
    public Step masterStep() {
        return new StepBuilder("masterStep", jobRepository)
                .partitioner("slaveStep", stockPartitioner())
                .step(slaveStep())
                .gridSize(GRID_SIZE)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Partitioner stockPartitioner() {
        return gridSize -> {
            Map<String, ExecutionContext> partitions = new HashMap<>();
            int pagesPerPartition = MAX_PAGE / GRID_SIZE;
            int remainder = MAX_PAGE % GRID_SIZE;
            int start = 1;

            for (int i = 0; i < GRID_SIZE; i++) {
                int size = pagesPerPartition + (i < remainder ? 1 : 0);
                int end = start + size - 1;   // 마지막 파티션의 end 값은 MAX_PAGE=10이 됨
                ExecutionContext ctx = new ExecutionContext();
                ctx.putInt("pageStart", start);
                ctx.putInt("pageEnd", end);
                partitions.put("partition" + i, ctx);
                start = end + 1;
            }

            return partitions;
        };
    }

    @Bean
    public Step slaveStep() {
        return new StepBuilder("slaveStep", jobRepository)
                .<Stock, Stock>chunk(pageSize, transactionManager)
                .reader(slaveItemReader(0, 0))
                .writer(stockItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Stock> slaveItemReader(
            @Value("#{stepExecutionContext['pageStart']}") int pageStart,
            @Value("#{stepExecutionContext['pageEnd']}")   int pageEnd
    ) {
        return new ItemReader<>() {
            private Iterator<Stock> iterator;

            @Override
            public Stock read() throws Exception {
                if (iterator == null) {
                    existingSymbols = new HashSet<>(stockRepository.findAllSymbols());

                    List<Stock> fetched = new ArrayList<>();
                    RestTemplate rt = restTemplate();

                    for (int page = pageStart; page <= pageEnd; page++) {
                        URI uri = URI.create(apiUrl
                                + "?serviceKey=" + serviceKey
                                + "&resultType=json"
                                + "&numOfRows=" + pageSize
                                + "&pageNo=" + page);
                        String json = rt.getForObject(uri, String.class);
                        if (json == null) continue;

                        JsonNode items = objectMapper.readTree(json)
                                .path("response").path("body").path("items").path("item");
                        if (items.isArray()) {
                            for (JsonNode item : items) {
                                String symbol = item.path("srtnCd").asText();
                                if (existingSymbols.contains(symbol)) continue;
                                existingSymbols.add(symbol);
                                fetched.add(Stock.builder()
                                        .symbol(symbol)
                                        .name(item.path("itmsNm").asText())
                                        .market(item.path("mrktCtg").asText())
                                        .isin(item.path("isinCd").asText())
                                        .build());
                            }
                        }
                    }

                    this.iterator = fetched.iterator();
                }

                return iterator.hasNext() ? iterator.next() : null;
            }
        };
    }

    @Bean
    public ItemWriter<Stock> stockItemWriter() {
        return (Chunk<? extends Stock> chunk) -> {
            List<? extends Stock> items = chunk.getItems();

            if (!items.isEmpty()) {
                List<Stock> list = new ArrayList<>(items);
                stockRepository.saveAll(list);
            }
        };
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(GRID_SIZE);
        executor.setMaxPoolSize(GRID_SIZE);
        executor.setQueueCapacity(GRID_SIZE);
        executor.afterPropertiesSet();
        return executor;
    }
}