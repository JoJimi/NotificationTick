package org.example.backend.domain.notification.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.notification.dto.response.NotificationResponse;
import org.example.backend.domain.notification.entity.Notification;
import org.example.backend.domain.notification.repository.NotificationRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Long userId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findPageByUserId(userId, pageable);
        return page.map(NotificationResponse::fromEntity);
    }

    @Transactional
    public boolean markAsRead(Long userId, Long notificationId) {
        return notificationRepository.markAsRead(notificationId, userId) > 0;
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }
}