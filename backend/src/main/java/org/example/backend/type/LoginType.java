package org.example.backend.type;

import lombok.Getter;
import java.util.*;

@Getter
public enum LoginType {     // 로그인 유형
    LOCAL("local"),
    KAKAO("kakao"),
    GOOGLE("google");

    private final String provider;

    LoginType(String provider) {
        this.provider = provider;
    }

    public static Optional<LoginType> fromProvider(String provider) {
        if (provider == null) {
            return Optional.empty();
        }
        String normalized = provider.trim().toLowerCase();
        return Arrays.stream(LoginType.values())
                .filter(e -> e.provider.equalsIgnoreCase(normalized))
                .findFirst();
    }

}
