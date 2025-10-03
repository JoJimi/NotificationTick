package org.example.backend.global.jwt.redis;

import lombok.RequiredArgsConstructor;
import org.example.backend.global.jwt.JwtProperties;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class TokenRepository {

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    // === 키 프리픽스 ===
    // 과거 단일 RT(String) 저장과 충돌을 원천 차단하려면 아래 SESSION_PREFIX를 "session:refresh:" 로 변경하고,
    // 마이그레이션 기간 동안 구키를 읽어 새 포맷으로 저장 후 삭제하는 로직을 추가하세요.
    private static final String SESSION_PREFIX = "refresh:";         // 과거와 동일 (타입 가드로 보호)
    private static final String BL_RT_PREFIX   = "blacklist:rt:";
    private static final String BL_FAM_PREFIX  = "blacklist:fam:";

    private String keySession(String userId) { return SESSION_PREFIX + userId; }
    private String keyRtBlacklist(String jti) { return BL_RT_PREFIX + jti; }
    private String keyFamBlacklist(String fam) { return BL_FAM_PREFIX + fam; }

    // === 내부 유틸: 키 타입 가드 ===
    /** 해당 key가 HASH가 아니면(STRING/SET/… 또는 NONE 제외) 삭제하여 HMSET WRONGTYPE 방지 */
    private void ensureHashKey(String key) {
        DataType type = redisTemplate.type(key);
        if (type != null && type != DataType.NONE && type != DataType.HASH) {
            redisTemplate.delete(key);
        }
    }

    // === 세션 상태 저장/조회 (familyId/currentJti/rotatedAt) ===
    public void saveSession(String userId, String familyId, String currentJti, long rotatedAtEpochMs, long ttlMs) {
        String key = keySession(userId);

        // 과거 문자열 키와 충돌 방지
        ensureHashKey(key);

        Map<String, String> map = new HashMap<>(4);
        map.put("familyId", familyId);
        map.put("currentJti", currentJti);
        map.put("rotatedAt", String.valueOf(rotatedAtEpochMs));

        redisTemplate.opsForHash().putAll(key, map);

        if (ttlMs > 0) {
            redisTemplate.expire(key, Duration.ofMillis(ttlMs));
        }
    }

    public RefreshSession findSession(String userId) {
        String key = keySession(userId);

        // 읽기 시에도 타입 점검: HASH가 아니면 세션 없음으로 간주(과격 삭제 대신 보수적 처리)
        DataType type = redisTemplate.type(key);
        if (type == null || type == DataType.NONE) return null;
        if (type != DataType.HASH) return null;

        Object fam = redisTemplate.opsForHash().get(key, "familyId");
        if (fam == null) return null;

        String familyId   = Objects.toString(fam, null);
        String currentJti = Objects.toString(redisTemplate.opsForHash().get(key, "currentJti"), null);
        String rotatedAtStr = Objects.toString(redisTemplate.opsForHash().get(key, "rotatedAt"), "0");
        long rotatedAt;
        try {
            rotatedAt = Long.parseLong(rotatedAtStr);
        } catch (NumberFormatException e) {
            rotatedAt = 0L;
        }
        return new RefreshSession(familyId, currentJti, rotatedAt);
    }

    public void deleteSession(String userId) {
        redisTemplate.delete(keySession(userId));
    }

    // === 블랙리스트(String 타입) ===
    public void blacklistJti(String jti, long ttlMs) {
        if (jti == null) return;
        if (ttlMs > 0) {
            redisTemplate.opsForValue().set(keyRtBlacklist(jti), "1", Duration.ofMillis(ttlMs));
        } else {
            redisTemplate.opsForValue().set(keyRtBlacklist(jti), "1");
        }
    }

    public boolean isJtiBlacklisted(String jti) {
        if (jti == null) return false;
        Boolean exists = redisTemplate.hasKey(keyRtBlacklist(jti));
        return exists != null && exists;
    }

    public void blacklistFamily(String fam, long ttlMs) {
        if (fam == null) return;
        if (ttlMs > 0) {
            redisTemplate.opsForValue().set(keyFamBlacklist(fam), "1", Duration.ofMillis(ttlMs));
        } else {
            redisTemplate.opsForValue().set(keyFamBlacklist(fam), "1");
        }
    }

    public boolean isFamilyBlacklisted(String fam) {
        if (fam == null) return false;
        Boolean exists = redisTemplate.hasKey(keyFamBlacklist(fam));
        return exists != null && exists;
    }
}