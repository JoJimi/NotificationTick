package org.example.backend.type;

import java.util.Arrays;
import java.util.Optional;

public enum AudienceType{
    PERSONAL("특정 사용자 한 명"),
    MULTICAST("조건에 맞는 여러 사용자"),
    BROADCAST("전체 사용자");

    private final String description;

    AudienceType(String description) {
        this.description = description;
    }

    public static Optional<AudienceType> fromName(String name) {
        if (name == null) {
            return Optional.empty();
        }

        String normalized = name.trim().toUpperCase();
        return Arrays.stream(AudienceType.values())
                .filter(t -> t.name().equals(normalized))
                .findFirst();
    }
}
