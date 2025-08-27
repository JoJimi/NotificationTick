package org.example.backend.domain.notification.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.notification.entity.Notification;
import org.example.backend.domain.notification.repository.NotificationRepository;
import org.example.backend.domain.notification.repository.SpringDataNotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final SpringDataNotificationRepository repository;

    @Override
    public List<Notification> findAllByUserId(Long userId) {
        return repository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Page<Notification> findPageByUserId(Long userId, Pageable pageable) {
        return repository.findPageByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public Optional<Notification> findByIdAndUserId(Long id, Long userId) {
        return repository.findByIdAndUser_Id(id, userId);
    }

    @Override
    public Notification save(Notification notification) {
        return repository.save(notification);
    }

    @Override
    public List<Notification> saveAll(List<Notification> notifications) {
        return repository.saveAll(notifications);
    }

    @Override
    public long countUnreadByUserId(Long userId) {
        return repository.countUnreadByUserId(userId);
    }

    @Override
    public int markAsRead(Long id, Long userId) {
        return repository.markAsRead(id, userId);
    }

    @Override
    public int markAllAsReadByUserId(Long userId) {
        return repository.markAllAsReadByUserId(userId);
    }
}