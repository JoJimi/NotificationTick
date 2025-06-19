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
// OAuth2 로그인 성공 핸들러
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
        /**
         * [** 트러블 슈팅 **]
         * Google: OIDC 방식 사용 -> OidcUserService가 실행되어 DefaultOidUser 반환
         * Kakao: 일반 OAuth2 방식 -> 우리가 등록한 CustomAuth2UserService가 실행되어 CustomOAuth2User 반환
         * 근데 다음 성공 핸들러에서 무조건 CustomOAuth2User로 캐스팅하기 때문에
         * google 로그인의 경우 DefaultOidUser 이므로 오류
         * 해결방안: 나눠서 분기별로 실행
         */
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        CustomOAuth2User oAuthUser;

        if (principal instanceof CustomOAuth2User customUser) {
            // 카카오 로그인
            oAuthUser = customUser;

        } else if (principal instanceof DefaultOidcUser oidcUser) {
            // 구글 로그인
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

        String userIdKey    = oAuthUser.getUser().getId().toString();
        String accessToken  = tokenProvider.generateAccessToken(userIdKey);
        String refreshToken = tokenProvider.generateRefreshToken(userIdKey);

        // Redis에는 이제 providerId 가 아니라 UserId를 String 형태를 키로 저장
        tokenService.storeRefreshToken(userIdKey, refreshToken);

        String redirectUri = UriComponentsBuilder
                .fromHttpUrl(frontBaseUrl + "/oauth2/callback")
                .queryParam("accessToken",  accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }
}
