package org.example.backend.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.notification.dto.response.NotificationResponse;
import org.example.backend.domain.notification.entity.Notification;
import org.example.backend.domain.notification.repository.adapter.NotificationRepositoryAdapter;
import org.example.backend.domain.notification.service.NotificationService;
import org.example.backend.global.jwt.custom.CustomUserDetails;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationRestController {

    private final NotificationService notificationService;

    /** 페이지 단위 알림 조회 (최신순). size=10 으로 호출하면 "더보기" UX에 맞음 */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable
    ) {
        Long userId = principal.getUser().getId();
        Page<NotificationResponse> response = notificationService.getMyNotifications(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /** 개별 알림 읽음 처리 */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long notificationId
    ) {
        Long userId = principal.getUser().getId();
        boolean updated = notificationService.markAsRead(userId, notificationId);
        return updated ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /** 전체 미읽음 알림 읽음 처리 */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long userId = principal.getUser().getId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
}