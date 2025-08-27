package org.example.backend.domain.notification.repository;

import org.example.backend.domain.notification.entity.Notification;
import org.example.backend.domain.notification.repository.query.NotificationQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface SpringDataNotificationRepository extends JpaRepository<Notification, Long>, NotificationQueryRepository {
    /**
     * 특정 사용자의 모든 알림을 생성일시 기준 내림차순으로 반환
     * - 목록 화면에서 전체 알림 조회 시 사용
     */
    List<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId);

    /**
     * 알림 소유자 검증용 단건 조회
     * - 읽음 처리 등에서 "요청자 == 소유자" 확인할 때 사용
     */
    Optional<Notification> findByIdAndUser_Id(Long id, Long userId);
}
