package org.example.backend.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.user.repository.UserRepository;
import org.example.backend.global.exception.user.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /**
     * 현재 로그인된 사용자의 정보를 조회합니다.
     */
    public User getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    /**
     * 사용자 닉네임을 수정합니다.
     */
    public User updateNickname(Long userId, String nickname) {
        User user = getCurrentUser(userId);
        user.setNickname(nickname);
        return userRepository.save(user);
    }

    /**
     * 현재 로그인된 사용자를 삭제(탈퇴)합니다.
     */
    public void deleteUser(Long userId) {
        userRepository.delete(getCurrentUser(userId));
    }
}
