package org.example.backend.type;

import lombok.Getter;
import java.util.*;

@Getter
public enum NotificationType {  // 알림 유형
    PRICE_DROP("급락 알림"),
    NEWS_EVENT("뉴스 발생 알림"),
    THRESHOLD("수익률/목표가 임계치 초과 알림");

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
