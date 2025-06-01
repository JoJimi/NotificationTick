package org.example.backend.type;

import lombok.Getter;
import java.util.*;

@Getter
public enum TradeType { // 거래 유형
    BUY("매수"),
    SELL("매도");

    private final String description;

    TradeType(String description) {
        this.description = description;
    }

    public static Optional<TradeType> fromName(String name) {
        if (name == null) {
            return Optional.empty();
        }

        String normalized = name.trim().toUpperCase();
        return Arrays.stream(TradeType.values())
                .filter(t -> t.name().equals(normalized))
                .findFirst();
    }
}
