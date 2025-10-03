package org.example.backend.domain.notification.repository;

import org.example.backend.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface NotificationRepository {

    /**
     * 특정 사용자의 모든 알림(최신순) 조회
     */
    List<Notification> findAllByUserId(Long userId);

    /**
     * 특정 사용자의 알림 페이징 조회(최신순)
     */
    Page<Notification> findPageByUserId(Long userId, Pageable pageable);

    /**
     * 단건 + 소유자 검증 조회
     */
    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    /**
     * 알림 저장
     */
    Notification save(Notification notification);

    /**
     * 알림 일괄 저장
     */
    List<Notification> saveAll(List<Notification> notifications);

    /**
     * 특정 사용자의 '미읽음' 알림 개수 조회
     */
    long countUnreadByUserId(Long userId);

    /**
     * 특정 알림 읽음 처리 (본인 소유 + 현재 미읽음인 건만)
     */
    int markAsRead(Long id, Long userId);

    /**
     * 해당 사용자의 모든 미읽음 알림 일괄 읽음 처리
     */
    int markAllAsReadByUserId(Long userId);

}
