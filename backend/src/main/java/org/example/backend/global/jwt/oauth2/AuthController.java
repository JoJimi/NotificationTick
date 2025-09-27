package org.example.backend.global.jwt.oauth2;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend.global.exception.auth.InvalidRefreshTokenException;
import org.example.backend.global.jwt.JwtTokenProvider;
import org.example.backend.global.jwt.custom.CustomUserDetails;
import org.example.backend.global.jwt.dto.request.TokenRequest;
import org.example.backend.global.jwt.dto.response.TokenResponse;
import org.example.backend.global.jwt.redis.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtTokenProvider jwtProvider;
    private final TokenService tokenService;

    /**
     * Refresh Token 검증 후 Access & Refresh Token 발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody TokenRequest request
    ) {
        String refreshToken = request.refreshToken();

        // 1) 토큰 서명·만료·토큰타입 검증
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        // 2) Redis에 저장된 토큰과 일치하는지 검증
        String userId;
        try {
            userId = jwtProvider.getSubject(refreshToken);
        } catch (JwtException exception) {
            throw new InvalidRefreshTokenException();
        }

        if (!tokenService.validateRefreshToken(userId, refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        // 3) 새로운 토큰 발급
        String accessToken = jwtProvider.generateAccessToken(userId);
        String newRefreshToken = jwtProvider.generateRefreshToken(userId);

        // 4) Redis에 새 Refresh Token 저장
        tokenService.storeRefreshToken(userId, newRefreshToken);

        // 5) 새 토큰 반환
        return ResponseEntity.ok(new TokenResponse(accessToken, newRefreshToken));
    }

    /**
     * 로그아웃: Redis에 저장된 Refresh Token 삭제
     */
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        String userId = principal.getUser().getId().toString();
        tokenService.deleteRefreshToken(userId);
        return ResponseEntity.noContent().build();
    }

}
