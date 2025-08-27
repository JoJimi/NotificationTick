package org.example.backend.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.backend.domain.notification.entity.Notification;
import org.example.backend.type.NotificationType;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Schema(description = "알림 응답 DTO")
public record NotificationResponse(

        @Schema(description = "알림 ID", example = "123")
        Long id,

        @Schema(description = "알림 유형", example = "NEWS_EVENT")
        NotificationType type,

        @Schema(description = "알림 내용", example = "'삼성전자'에서 최신 뉴스 \"2분기 실적발표\"이 업로드되었습니다.")
        String message,

        @Schema(description = "읽음 여부", example = "false")
        boolean isRead,

        @Schema(description = "알림 생성 시각")
        LocalDateTime createdAt,

        @Schema(description = "사용자 닉네임", example = "yeongung")
        String nickname
) {
    public static NotificationResponse fromEntity(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getUser() != null ? notification.getUser().getNickname() : null
        );
    }
}