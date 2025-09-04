package org.example.backend.type;

import lombok.Getter;
import java.util.*;

@Getter
public enum NotificationType {  // 알림 유형
    PRICE_DROP("급락 알림 -5%이하"),
    PRICE_SURGE("급등 알림 +5%이상"),
    NEWS_EVENT("뉴스 발생 알림"),
    THRESHOLD("수익률/목표가 임계치 초과 알림"),
    INTEREST_ADDED("관심종목 등록");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public static Optional<NotificationType> fromName(String code) {
        if (code == null) {
            return Optional.empty();
        }

        String normalized = code.trim().toUpperCase();
        return Arrays.stream(NotificationType.values())
                .filter(e -> e.name().equals(normalized))
                .findFirst();
    }
}
