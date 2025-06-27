package org.example.backend.domain.user.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.user.repository.SpringDataUserRepository;
import org.example.backend.domain.user.repository.UserRepository;
import org.example.backend.type.LoginType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    @Override
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByLoginTypeAndProviderId(LoginType loginType, String providerId) {
        return springDataUserRepository.findByLoginTypeAndProviderId(loginType, providerId);
    }

    @Override
    public User save(User user) {
        return springDataUserRepository.save(user);
    }

    @Override
    public void delete(User user) {
        springDataUserRepository.delete(user);
    }
}
