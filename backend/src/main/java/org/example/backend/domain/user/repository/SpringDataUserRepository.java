package org.example.backend.domain.user.repository;

import org.example.backend.domain.user.entity.User;
import org.example.backend.type.LoginType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    /**
     * 소셜 로그인에서 내려준 providerId와 loginType(Google/Kakao)으로 조회
     */
    Optional<User> findByLoginTypeAndProviderId(LoginType loginType, String providerId);

}
