package org.example.backend.global.jwt.oauth2;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.global.exception.auth.InvalidRefreshTokenException;
import org.example.backend.global.jwt.JwtTokenProvider;
import org.example.backend.global.jwt.custom.CustomUserDetails;
import org.example.backend.global.jwt.dto.request.TokenRequest;
import org.example.backend.global.jwt.dto.response.TokenResponse;
import org.example.backend.global.jwt.redis.RefreshSession;
import org.example.backend.global.jwt.redis.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtTokenProvider jwtProvider;
    private final TokenService tokenService;

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody TokenRequest request) {
        String refreshToken = request.refreshToken();

        // 1) 서명/만료/타입 검증
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        // 2) 클레임 추출
        final String userId;
        final String fam;
        final String jti;
        final Date   exp;
        try {
            userId = jwtProvider.getSubject(refreshToken);
            fam    = jwtProvider.getFamilyId(refreshToken);
            jti    = jwtProvider.getJti(refreshToken);
            exp    = jwtProvider.getExpiration(refreshToken);
        } catch (JwtException e) {
            throw new InvalidRefreshTokenException();
        }

        long nowMs = System.currentTimeMillis();
        long remainTtlMs = exp.getTime() - nowMs;

        // 3) 블랙리스트 확인 (이미 회전된 RT 사용/가족 차단)
        if (tokenService.isJtiBlacklisted(jti) || tokenService.isFamilyBlacklisted(fam)) {
            throw new InvalidRefreshTokenException();
        }

        // 4) 서버 세션 상태 확인
        RefreshSession session = tokenService.getSession(userId);
        if (session == null) throw new InvalidRefreshTokenException();

        // 세션군(fam) 불일치 → 의심 시나리오 (장치가 다르거나 변조)
        if (!fam.equals(session.familyId())) {
            throw new InvalidRefreshTokenException();
        }

        // 5) 회전/재사용 분기
        if (!jti.equals(session.currentJti())) {
            // 재사용 탐지: 과거 RT 재제출
            tokenService.blacklistReuseAndInvalidate(userId, fam, jti, remainTtlMs);
            throw new InvalidRefreshTokenException();
        }

        // 정상: 회전 수행
        String newAccessToken  = jwtProvider.generateAccessToken(userId, fam);
        String newRefreshToken = jwtProvider.generateRefreshToken(userId, fam);
        String newJti          = jwtProvider.getJti(newRefreshToken);

        // 이전 jti 블랙리스트 + 세션 갱신
        long newTtlMs = jwtProvider.getExpiration(newRefreshToken).getTime() - nowMs;
        tokenService.rotate(userId, fam, jti, newJti, remainTtlMs, newTtlMs);

        return ResponseEntity.ok(new TokenResponse(newAccessToken, newRefreshToken));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CustomUserDetails principal) {
        String userId = principal.getUser().getId().toString();
        tokenService.deleteSession(userId);
        return ResponseEntity.noContent().build();
    }
}
