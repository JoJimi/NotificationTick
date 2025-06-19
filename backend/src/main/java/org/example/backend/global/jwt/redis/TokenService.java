package org.example.backend.global.jwt.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
// RefreshToken 관리 서비스
public class TokenService {

    private final TokenRepository tokenRepository;

    // 사용자별 리프레시 토큰 저장
    public void storeRefreshToken(String userIdKey, String refreshToken) {
        tokenRepository.save(userIdKey, refreshToken);
    }

    // 토큰 유효성 검사 (Redis에 저장된 토큰과 일치하는지 확인)
    public boolean validateRefreshToken(String userIdKey, String refreshToken) {
        String storedToken = tokenRepository.findByKey(userIdKey);
        if(storedToken == null) return false;
        return refreshToken.equals(storedToken);
    }

    // 리프레시 토큰 삭제 (로그아웃 또는 재발급 시 사용)
    public void deleteRefreshToken(String username) {
        tokenRepository.delete(username);
    }
}
