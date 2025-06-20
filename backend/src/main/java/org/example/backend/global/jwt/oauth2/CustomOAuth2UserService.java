package org.example.backend.global.jwt.oauth2;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.user.entity.User;
import org.example.backend.type.LoginType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * OAuth2UserService 구현체:
 *  - 일반 OAuth2(OAuth2UserRequest) 와 OIDC(OidcUserRequest) 모두 처리
 *  - User 등록/조회/수정 로직은 OAuth2UserRegistration 빈에 위임
 *  - 최종적으로 CustomOAuth2User 반환
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate;
    private final OAuth2UserRegistration registration;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1) 프로바이더(카카오)에서 사용자 정보 로드
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();

        // 2) 로그인 타입 판별 (GOOGLE/KAKAO 중 KAKAO)
        LoginType loginType = LoginType.fromProvider(userRequest.getClientRegistration().getRegistrationId())
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        new OAuth2Error("invalid_provider", "지원하지 않는 로그인 제공자: ", null)
                ));

        // 3) providerId 추출 (카카오 응답의 user-name-attribute, 보통 "id")
        String userNameAttr = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        String providerId = Optional.ofNullable(attributes.get(userNameAttr))
                .map(Object::toString)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        new OAuth2Error("invalid_provider_id", "프로바이더 ID가 응답에 없습니다", null)
                ));

        // 4) 이메일 & 닉네임 추출 (카카오 계정 구조)
        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = Optional.ofNullable(kakaoAccount.get("email"))
                .map(Object::toString)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        new OAuth2Error("no_email", "Kakao 계정에 이메일 동의가 필요합니다", null)
                ));
        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = Optional.ofNullable(kakaoProfile.get("nickname"))
                .map(Object::toString)
                .filter(StringUtils::hasText)
                .orElse("Unknown");

        // 5) DB에 조회 or 새로 생성 후 프로필 업데이트
        User user = registration.registerOrUpdate(loginType, providerId, email, nickname);

        // 6) 권한 부여
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(user.getRole().name())
        );

        // 7) CustomOAuth2User 반환
        return new CustomOAuth2User(user, attributes, authorities);
    }
}
