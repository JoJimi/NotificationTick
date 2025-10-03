package org.example.backend.global.jwt.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.global.jwt.JwtTokenProvider;
import org.example.backend.global.jwt.redis.TokenService;
import org.example.backend.type.LoginType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final TokenService tokenService;
    private final OAuth2UserRegistration registration;

    @Value("${app.frontBaseUrl}")
    private String frontBaseUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        CustomOAuth2User oAuthUser;

        if (principal instanceof CustomOAuth2User customUser) {
            oAuthUser = customUser;
        } else if (principal instanceof DefaultOidcUser oidcUser) {
            oAuthUser = new CustomOAuth2User(
                    registration.registerOrUpdate(
                            LoginType.GOOGLE,
                            oidcUser.getSubject(),
                            oidcUser.getAttribute("email"),
                            oidcUser.getAttribute("name")
                    ),
                    oidcUser.getAttributes(),
                    oidcUser.getAuthorities()
            );
        } else {
            throw new IllegalStateException("지원하지 않는 principal 타입");
        }

        String userId = oAuthUser.getUser().getId().toString();

        // 신규 세션군(family) 생성
        String familyId = tokenProvider.newFamilyId();

        // 토큰 발급
        String accessToken  = tokenProvider.generateAccessToken(userId, familyId);
        String refreshToken = tokenProvider.generateRefreshToken(userId, familyId);

        // jti 추출 후 세션 저장
        String jti = tokenProvider.getJti(refreshToken);
        tokenService.initSession(userId, familyId, jti);

        // (기존과 동일) 쿼리스트링 전달 — 운영시 HttpOnly 쿠키/교환 엔드포인트 권장
        String redirectUri = UriComponentsBuilder
                .fromHttpUrl(frontBaseUrl + "/oauth2/callback")
                .queryParam("accessToken",  accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        log.info("OAuth2 redirect: {}", redirectUri);
        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }
}
