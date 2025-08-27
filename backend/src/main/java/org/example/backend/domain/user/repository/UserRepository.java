package org.example.backend.domain.user.repository;

import org.example.backend.domain.user.entity.User;
import org.example.backend.type.LoginType;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long id);
    Optional<User> findByLoginTypeAndProviderId(LoginType loginType, String providerId);
    User save(User user);
    void delete(User user);

}
