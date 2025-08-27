package org.example.backend.domain.notification.repository.query;

import org.example.backend.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface NotificationQueryRepository {

    /**
     * 특정 사용자 알림을 생성일시 기준 내림차순으로 페이징 조회
     */
    Page<Notification> findPageByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 특정 사용자의 미읽음 알림 개수 조회
     */
    long countUnreadByUserId(Long userId);

    /**
     * 단건 읽음 처리 (본인 소유 + 현재 미읽음 조건)
     */
    int markAsRead(Long id, Long userId);

    /**
     * 해당 사용자의 모든 미읽음 알림 일괄 읽음 처리
     */
    int markAllAsReadByUserId(Long userId);

}
