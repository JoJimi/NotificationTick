package org.example.backend.domain.notification.kafka;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.notification.dto.NotificationEvent;
import org.example.backend.domain.stock.entity.Stock;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private static final String TOPIC = "notifications";

    /** 관심종목 등록 이벤트 발행 */
    public void sendInterestAdded(Long userId, Stock stock) {
        NotificationEvent event = NotificationEvent.ofInterestAdded(
                userId,
                stock.getId(),
                stock.getName()
        );
        kafkaTemplate.send(TOPIC, event);
    }

    /** 뉴스 발생 이벤트 발행 */
    public void sendNewsEvent(Stock stock, String newsTitle) {
        NotificationEvent event = NotificationEvent.ofNewsEvent(
                stock.getId(),
                stock.getName(),
                newsTitle
        );
        kafkaTemplate.send(TOPIC, event);
    }

    /** 관심 등록 시 관심종목 급등 이벤트 발행 */
    public void sendPriceSurgeByInterestAddedEvent(Long userId, Stock stock) {
        NotificationEvent event = NotificationEvent.ofPriceSurgeByInterestAddedEvent(
                userId,
                stock.getId(),
                stock.getName(),
                stock.getChangeRate()
        );
        kafkaTemplate.send(TOPIC, event);
    }

    /** 관심 등록 시 관심종목 급락 이벤트 발행 */
    public void sendPriceDropByInterestAddedEvent(Long userId, Stock stock) {
        NotificationEvent event = NotificationEvent.ofPriceDropByInterestAddedEvent(
                userId,
                stock.getId(),
                stock.getName(),
                stock.getChangeRate()
        );
        kafkaTemplate.send(TOPIC, event);
    }

    /** 특정 시간에 관심종목 급등 이벤트 발행 */
    public void sendPriceSurgeByScheduledEvent(Stock stock) {
        NotificationEvent event = NotificationEvent.ofPriceSurgeByScheduledEvent(
                stock.getId(),
                stock.getName(),
                stock.getChangeRate()
        );
        kafkaTemplate.send(TOPIC, event);
    }

    /** 특정 시간에 관심종목 급락 이벤트 발행 */
    public void sendPriceDropByScheduledEvent(Stock stock) {
        NotificationEvent event = NotificationEvent.ofPriceDropByScheduledEvent(
                stock.getId(),
                stock.getName(),
                stock.getChangeRate()
        );
        kafkaTemplate.send(TOPIC, event);
    }
}