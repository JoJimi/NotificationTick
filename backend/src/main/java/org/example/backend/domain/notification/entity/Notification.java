package org.example.backend.domain.notification.entity;

import jakarta.persistence.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.user.entity.User;
import org.example.backend.global.entity.BaseEntity;
import org.example.backend.type.NotificationType;

@Entity
@Table(name = "notification")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {      /** 알림 **/

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 알림 수신 대상 사용자 (NOT NULL) **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 관련 종목 - 선택적 FK **/
    @Column(name = "stock_id")
    private Long stockId;

    /** 알림 유형 (NOT NULL)
     * PRICE_DROP("급락 알림"),
     * NEWS_EVENT("뉴스 발생 알림"),
     * THRESHOLD("수익률/목표가 임계치 초과 알림") **/
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    /** 알림 메시지 (NOT NULL) **/
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /** 읽음 여부 (NOT NULL, DEFAULT FALSE) **/
    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

}
