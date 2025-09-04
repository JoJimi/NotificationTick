package org.example.backend.global.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.stock.dto.batch.StockUpsert;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.net.URI;
import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StockDataBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final RestTemplateBuilder restTemplateBuilder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openapi.apiUrl}")
    private String apiUrl;

    @Value("${openapi.serviceKey}")
    private String serviceKey;

    @Value("${openapi.page-size:1000}")
    private int pageSize;

    private static final int MAX_PAGE = 10;
    private static final int GRID_SIZE = 5;

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
                int end = start + size - 1;
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
                .<StockUpsert, StockUpsert>chunk(pageSize, transactionManager)
                .reader(slaveItemReader(0, 0))
                .writer(stockUpsertWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<StockUpsert> slaveItemReader(
            @Value("#{stepExecutionContext['pageStart']}") int pageStart,
            @Value("#{stepExecutionContext['pageEnd']}")   int pageEnd
    ) {
        return new ItemReader<>() {
            private Iterator<StockUpsert> iterator;

            @Override
            public StockUpsert read() throws Exception {
                if (iterator == null) {
                    RestTemplate restTemplate = restTemplate();

                    Set<String> seenInPartition = new HashSet<>();
                    List<StockUpsert> fetched = new ArrayList<>();

                    for (int page = pageStart; page <= pageEnd; page++) {
                        URI uri = URI.create(apiUrl
                                + "?serviceKey=" + serviceKey
                                + "&resultType=json"
                                + "&numOfRows=" + pageSize
                                + "&pageNo=" + page);

                        String json = restTemplate.getForObject(uri, String.class);
                        if (json == null || json.isBlank()) continue;

                        JsonNode items = objectMapper.readTree(json)
                                .path("response").path("body").path("items").path("item");

                        if (items.isArray()) {
                            for (JsonNode item : items) {
                                String symbol = item.path("srtnCd").asText();
                                if (symbol.isBlank() || !seenInPartition.add(symbol)) continue;

                                String name = item.path("itmsNm").asText();
                                String market = item.path("mrktCtg").asText();
                                String isin = item.path("isinCd").asText();

                                String fltRtStr = item.path("fltRt").asText();
                                String volumeStr = item.path("trqu").asText();

                                Double changeRate = 0.0;
                                Long volume = 0L;
                                try {
                                    if (!fltRtStr.isBlank()) changeRate = Double.parseDouble(fltRtStr);
                                    if (!volumeStr.isBlank()) volume = Long.parseLong(volumeStr);
                                } catch (NumberFormatException e) {
                                    log.warn("Parse error: symbol={}, fltRt='{}', trqu='{}'", symbol, fltRtStr, volumeStr);
                                }

                                fetched.add(new StockUpsert(symbol, name, market, isin, changeRate, volume));
                            }
                        }
                    }
                    this.iterator = fetched.iterator();
                }
                return (iterator != null && iterator.hasNext()) ? iterator.next() : null;
            }
        };
    }

    @Bean
    public JdbcBatchItemWriter<StockUpsert> stockUpsertWriter() {
        String sql = """
            INSERT INTO stock(symbol, name, market, isin, change_rate, volume)
            VALUES (:symbol, :name, :market, :isin, :changeRate, :volume)
            ON CONFLICT (symbol) DO UPDATE
            SET name        = COALESCE(NULLIF(EXCLUDED.name, ''), stock.name),
                market      = COALESCE(NULLIF(EXCLUDED.market, ''), stock.market),
                isin        = COALESCE(NULLIF(EXCLUDED.isin, ''), stock.isin),
                change_rate = EXCLUDED.change_rate,
                volume      = EXCLUDED.volume
        """;

        return new JdbcBatchItemWriterBuilder<StockUpsert>()
                .dataSource(dataSource)
                .sql(sql)
                .beanMapped()  // StockUpsert의 accessor 이름(:symbol 등)로 바인딩
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(GRID_SIZE);
        executor.setMaxPoolSize(GRID_SIZE);
        executor.setQueueCapacity(GRID_SIZE);
        executor.setThreadNamePrefix("stock-batch-");
        executor.afterPropertiesSet();
        return executor;
    }
}
