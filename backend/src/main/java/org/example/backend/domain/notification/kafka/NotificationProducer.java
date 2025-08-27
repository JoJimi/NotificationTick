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
}