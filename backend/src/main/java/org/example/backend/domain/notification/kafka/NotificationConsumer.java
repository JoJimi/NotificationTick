package org.example.backend.domain.notification.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.notification.dto.NotificationEvent;
import org.example.backend.domain.notification.entity.Notification;
import org.example.backend.domain.notification.repository.NotificationRepository;
import org.example.backend.domain.notification.service.NotificationSseService;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.user.repository.UserRepository;
import org.example.backend.domain.watch_list.entity.WatchList;
import org.example.backend.domain.watch_list.repository.WatchListRepository;
import org.example.backend.type.NotificationType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final WatchListRepository watchListRepository;
    private final UserRepository userRepository;
    private final NotificationSseService sseService;

    @Transactional
    @KafkaListener(topics = "notifications", groupId = "notification-group")
    public void consume(NotificationEvent event) {
        // 커밋 후 푸시할 알림 모아두기
        List<Notification> toPush = new ArrayList<>();

        if (event.type() == NotificationType.INTEREST_ADDED) {
            // 관심등록: 대상 사용자 1명에게 생성
            User user = userRepository.findById(event.userId()).orElse(null);
            if (user == null) {
                log.warn("INTEREST_ADDED: user not found. userId={}", event.userId());
            } else {
                String msg = String.format("'%s'에 관심을 표시했습니다.", event.stockName());
                Notification n = Notification.builder()
                        .user(user)
                        .stock(Stock.builder().id(event.stockId()).build())
                        .type(NotificationType.INTEREST_ADDED)
                        .message(msg)
                        .build();
                notificationRepository.save(n);
                toPush.add(n);
            }
        } else if (event.type() == NotificationType.NEWS_EVENT) {
            // 뉴스: 해당 종목을 관심등록한 모든 사용자에게 생성
            List<WatchList> watchers = watchListRepository.findByStockId(event.stockId());
            if (watchers.isEmpty()) {
                log.debug("NEWS_EVENT: no watchers for stockId={}", event.stockId());
            } else {
                for (WatchList w : watchers) {
                    User user = w.getUser();
                    String msg = String.format("'%s'에서 최신 뉴스 \"%s\"이 업로드되었습니다.",
                            event.stockName(), event.newsTitle());
                    Notification n = Notification.builder()
                            .user(user)
                            .stock(Stock.builder().id(event.stockId()).build())
                            .type(NotificationType.NEWS_EVENT)
                            .message(msg)
                            .build();
                    notificationRepository.save(n);
                    toPush.add(n);
                }
            }
        }

        // 트랜잭션 커밋된 뒤에만 SSE로 푸시 (일관성 보장)
        if (!toPush.isEmpty()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    for (Notification n : toPush) {
                        try {
                            sseService.push(n.getUser().getId(), n);
                        } catch (Exception e) {
                            log.debug("SSE push failed userId={} notiId={}", n.getUser().getId(), n.getId(), e);
                        }
                    }
                }
            });
        }
    }
}
