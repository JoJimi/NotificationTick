package org.example.backend.type;

import lombok.Getter;
import java.util.*;

@Getter
public enum RoleType {  // 역할 유형
    ROLE_USER("USER"),
    ROLE_ADMIN("ADMIN");

    private final String code;

    RoleType(String code) {
        this.code = code;
    }

    public static Optional<RoleType> fromCode(String code) {
        if (code == null) {
            return Optional.empty();
        }

        String normalized = code.trim().toUpperCase();
        return Arrays.stream(RoleType.values())
                .filter(r -> r.code.equals(normalized))
                .findFirst();
    }
}
