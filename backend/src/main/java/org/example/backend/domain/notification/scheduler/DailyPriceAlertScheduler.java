package org.example.backend.domain.notification.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.notification.dto.NotificationEvent;
import org.example.backend.domain.notification.kafka.NotificationProducer;
import org.example.backend.domain.stock.repository.StockRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyPriceAlertScheduler {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final NotificationProducer producer;
    private final StockRepository stockRepository;

    @Value("${alert.price-threshold:5.0}")
    private double threshold;

    @Scheduled(cron = "0 0 9 * * MON-FRI", zone = "Asia/Seoul")
    public void publishDailyPriceAlerts() {
        // +5% 이상
        stockRepository.findByChangeRateGte(threshold)
                .forEach(producer::sendPriceSurgeByScheduledEvent);

        // -5% 이하
        stockRepository.findByChangeRateLte(-threshold)
                .forEach(producer::sendPriceDropByScheduledEvent);

        log.info("Triggered daily price alerts (threshold ±{}%)", threshold);
    }
}

