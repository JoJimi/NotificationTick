package org.example.backend.global.jwt.oauth2;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.user.repository.UserRepository;
import org.example.backend.type.LoginType;
import org.example.backend.type.RoleType;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OAuth2UserRegistration {

    private final UserRepository userRepository;

    /**
     * 주어진 provider 정보로 User를 조회하고, 없으면 생성, 있으면 프로필(email, nickname) 업데이트
     */
    public User registerOrUpdate(
            LoginType loginType,
            String providerId,
            String email,
            String nickname
    ) {
        return userRepository.findByLoginTypeAndProviderId(loginType, providerId)
                .map(existing -> {
                    boolean dirty = false;
                    if (!Objects.equals(existing.getEmail(), email)) {
                        existing.setEmail(email);
                        dirty = true;
                    }
                    if (!Objects.equals(existing.getNickname(), nickname)) {
                        existing.setNickname(nickname);
                        dirty = true;
                    }
                    return dirty
                            ? userRepository.save(existing)
                            : existing;
                })
                .orElseGet(() -> {
                    User user = User.builder()
                            .loginType(loginType)
                            .providerId(providerId)
                            .email(email)
                            .nickname(nickname)
                            .role(RoleType.ROLE_USER)
                            .build();
                    return userRepository.save(user);
                });
    }
}
