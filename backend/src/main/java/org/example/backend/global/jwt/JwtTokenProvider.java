package org.example.backend.global.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
// Access & Refresh 토큰 생성 및 검증 로직, 토큰 만료시간 설정
public class JwtTokenProvider implements InitializingBean {

    private final JwtProperties jwtProperties;
    private Key key;

    @Override
    public void afterPropertiesSet() {
        // Base64 또는 Hex 포맷 모두 수용
        String raw = jwtProperties.getJwtSecretKey();
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(raw);
        } catch (IllegalArgumentException e) {
            keyBytes = HexFormat.of().parseHex(raw);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Access Token 생성
    public String generateAccessToken(String userId, String familyId) {
        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("token_type", "accessToken");
        claims.put("fam", familyId);

        Date now = new Date();
        Date validity = new Date((new Date()).getTime() + jwtProperties.getAccessTokenExpirationMs());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)                                   // 시작 시각
                .setExpiration(validity)                            // 만료 시각
                .signWith(key, SignatureAlgorithm.HS256)            // 서명
                .compact();
    }

    @Deprecated
    public String generateAccessToken(String userId){
        return generateAccessToken(userId, newFamilyId());
    }

    // Refresh Token 생성
    public String generateRefreshToken(String userId, String familyId){
        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("token_type", "refreshToken");
        claims.put("fam", familyId);
        claims.put("jti", UUID.randomUUID().toString());


        Date now = new Date();
        Date validity = new Date((new Date()).getTime() + jwtProperties.getRefreshTokenExpirationMs());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)                                   // 시작 시각
                .setExpiration(validity)                            // 만료 시각
                .signWith(key, SignatureAlgorithm.HS256)            // 서명
                .compact();
    }

    // Access Token 검증
    public boolean validateAccessToken(String accessToken){
        try {
            Claims claims = parseClaims(accessToken);
            String tokenType = (String) claims.get("token_type");

            if (!tokenType.equals("accessToken")) {
                log.warn("토큰 타입(accessToken) 불일치");
                return false;
            }
            return !claims.getExpiration().before(new Date());

        } catch (SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다.", e);
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 입니다.", e);
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 입니다.", e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT가 잘못되었습니다.", e);
        }
        return false;
    }

    // Refresh Token 검증
    public boolean validateRefreshToken(String refreshToken){
        try {
            Claims claims = parseClaims(refreshToken);
            String tokenType = (String) claims.get("token_type");

            if (!tokenType.equals("refreshToken")) {
                log.warn("토큰 타입(refreshToken) 불일치");
                return false;
            }
            return !claims.getExpiration().before(new Date());

        } catch (SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다.", e);
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 입니다.", e);
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 입니다.", e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT가 잘못되었습니다.", e);
        }
        return false;
    }

    public String getSubject(String token) { return parseClaims(token).getSubject(); }
    public String getFamilyId(String token) { return String.valueOf(parseClaims(token).get("fam")); }
    public String getJti(String token) { return String.valueOf(parseClaims(token).get("jti")); }
    public Date getExpiration(String token) { return parseClaims(token).getExpiration(); }

    public String newFamilyId() { return UUID.randomUUID().toString(); }

    /**
     * JWT 파싱 메서드
     * - 서명 검증 + 만료 확인
     * - 정상이라면 Claims 반환
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)    // 서명 검증 키
                .build()
                .parseClaimsJws(token) // 실패 시 예외 발생
                .getBody();
    }

}
