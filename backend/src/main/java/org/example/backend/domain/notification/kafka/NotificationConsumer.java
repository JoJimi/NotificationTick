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
import org.example.backend.type.AudienceType;
import org.example.backend.type.NotificationType;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${alert.price-threshold}")
    private double priceThreshold;

    private final NotificationRepository notificationRepository;
    private final WatchListRepository watchListRepository;
    private final UserRepository userRepository;
    private final NotificationSseService sseService;

    private final org.example.backend.domain.stock.repository.StockRepository stockRepository;

    @Transactional
    @KafkaListener(topics = "notifications", groupId = "notification-group")
    public void consume(NotificationEvent event) {
        List<Notification> toPush = new ArrayList<>();

        if (event.type() == NotificationType.INTEREST_ADDED && event.audienceType() == AudienceType.PERSONAL) {
            handlePersonalInterestAdded(event, toPush);
        } else if (event.type() == NotificationType.NEWS_EVENT && event.audienceType() == AudienceType.MULTICAST) {
            handleNewsMulticast(event, toPush);
        } else if (event.type() == NotificationType.PRICE_DROP && event.audienceType() == AudienceType.PERSONAL) {
            handlePersonalPrice(event, toPush, NotificationType.PRICE_DROP);
        } else if (event.type() == NotificationType.PRICE_SURGE && event.audienceType() == AudienceType.PERSONAL) {
            handlePersonalPrice(event, toPush, NotificationType.PRICE_SURGE);
        } else if (event.audienceType() == AudienceType.MULTICAST
                && (event.type() == NotificationType.PRICE_SURGE || event.type() == NotificationType.PRICE_DROP)) {

            List<Stock> candidates = stockRepository.findByAbsChangeRateGte(priceThreshold);
            if (candidates.isEmpty()) {
                log.debug("MULTICAST {}: no stocks over ±{}%", event.type(), priceThreshold);
            } else {
                List<Notification> buffer = new ArrayList<>();

                for (Stock stock : candidates) {
                    Double changeRate = stock.getChangeRate();
                    if (changeRate == null) continue;

                    boolean isSurge = changeRate >= priceThreshold;
                    boolean isDrop  = changeRate <= -priceThreshold;

                    if ((event.type() == NotificationType.PRICE_SURGE && !isSurge) ||
                            (event.type() == NotificationType.PRICE_DROP  && !isDrop)) {
                        continue;
                    }

                    List<WatchList> watchers = watchListRepository.findByStockId(stock.getId());
                    if (watchers.isEmpty()) continue;

                    String msg = formatPriceMsg(stock.getName(), changeRate, isSurge);

                    for (WatchList wl : watchers) {
                        buffer.add(Notification.builder()
                                .user(wl.getUser())
                                .stock(stock)
                                .type(isSurge ? NotificationType.PRICE_SURGE : NotificationType.PRICE_DROP)
                                .message(msg)
                                .build());
                    }
                }

                if (!buffer.isEmpty()) {
                    notificationRepository.saveAll(buffer);
                    toPush.addAll(buffer);
                }
            }
        }

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

    private void handlePersonalInterestAdded(NotificationEvent event, List<Notification> toPush) {
        User user = userRepository.findById(event.userId()).orElse(null);
        if (user == null) {
            log.warn("INTEREST_ADDED: user not found. userId={}", event.userId());
            return;
        }
        String msg = String.format("'%s'에 관심을 표시했습니다.", event.stockName());
        Notification notification = Notification.builder()
                .user(user)
                .stock(Stock.builder().id(event.stockId()).build())
                .type(NotificationType.INTEREST_ADDED)
                .message(msg)
                .build();
        notificationRepository.save(notification);
        toPush.add(notification);
    }

    private void handleNewsMulticast(NotificationEvent event, List<Notification> toPush) {
        List<WatchList> watchers = watchListRepository.findByStockId(event.stockId());
        for (WatchList watchList : watchers) {
            User user = watchList.getUser();
            String msg = String.format("'%s'에서 최신 뉴스 \"%s\"이 업로드되었습니다.", event.stockName(), event.newsTitle());
            Notification notification = Notification.builder()
                    .user(user)
                    .stock(Stock.builder().id(event.stockId()).build())
                    .type(NotificationType.NEWS_EVENT)
                    .message(msg)
                    .build();
            notificationRepository.save(notification);
            toPush.add(notification);
        }
    }

    private void handlePersonalPrice(NotificationEvent event, List<Notification> toPush, NotificationType type) {
        User user = userRepository.findById(event.userId()).orElse(null);
        if (user == null) {
            log.warn("{}: user not found. userId={}", type, event.userId());
            return;
        }
        boolean surge = (type == NotificationType.PRICE_SURGE);
        String msg = formatPriceMsg(event.stockName(), event.changeRate(), surge);
        Notification notification = Notification.builder()
                .user(user)
                .stock(Stock.builder().id(event.stockId()).build())
                .type(type)
                .message(msg)
                .build();
        notificationRepository.save(notification);
        toPush.add(notification);
    }

    private String formatPriceMsg(String stockName, Double changeRate, boolean surge) {
        double cr = Optional.ofNullable(changeRate).orElse(0.0);
        String direction = surge ? "급등" : "급락";
        // 예: 종목 '삼성전자'가 5.23%로 급등했습니다.
        return String.format("종목 '%s'가 %.2f%%로 %s했습니다.", stockName, Math.abs(cr), direction);
    }
}
