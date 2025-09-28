package org.example.backend.global.jwt.redis;

import lombok.RequiredArgsConstructor;
import org.example.backend.global.jwt.JwtProperties;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository repo;
    private final JwtProperties props;

    // 신규/최초 세션 저장(로그인 시)
    public void initSession(String userId, String familyId, String currentJti) {
        repo.saveSession(userId, familyId, currentJti, System.currentTimeMillis(),
                props.getRefreshTokenExpirationMs());
    }

    public RefreshSession getSession(String userId) {
        return repo.findSession(userId);
    }

    public void rotate(String userId, String familyId, String oldJti, String newJti, long oldTtlMs, long newTtlMs) {
        // 이전 jti 블랙리스트(남은 TTL 만큼)
        repo.blacklistJti(oldJti, Math.max(oldTtlMs, 0));
        // 새 jti로 세션 갱신
        repo.saveSession(userId, familyId, newJti, System.currentTimeMillis(), newTtlMs);
    }

    public void blacklistReuseAndInvalidate(String userId, String familyId, String jti, long ttlMs) {
        // 재사용된 jti 블랙리스트 + 해당 세션군 차단(선택)
        repo.blacklistJti(jti, Math.max(ttlMs, 0));
        repo.blacklistFamily(familyId, Math.max(ttlMs, 0)); // 선택: fam 차단
        // 세션 삭제(해당 세션군 강제 로그아웃 효과)
        repo.deleteSession(userId);
    }

    public boolean isJtiBlacklisted(String jti) { return repo.isJtiBlacklisted(jti); }
    public boolean isFamilyBlacklisted(String fam) { return repo.isFamilyBlacklisted(fam); }

    public void deleteSession(String userId) { repo.deleteSession(userId); }
}
