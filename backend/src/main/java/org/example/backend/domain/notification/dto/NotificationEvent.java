package org.example.backend.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.backend.type.NotificationType;

@Schema(description = "Kafka 알림 이벤트 DTO")
public record NotificationEvent(

        @Schema(description = "알림 유형", example = "INTEREST_ADDED")
        NotificationType type,

        @Schema(description = "사용자 ID (관심등록 이벤트일 때 사용)", example = "42")
        Long userId,

        @Schema(description = "관련 종목 ID", example = "101")
        Long stockId,

        @Schema(description = "종목 이름", example = "삼성전자")
        String stockName,

        @Schema(description = "뉴스 제목 (뉴스 이벤트일 때 사용)", example = "삼성전자 2분기 실적 발표")
        String newsTitle
) {
    /** 관심종목 등록 이벤트 생성 */
    public static NotificationEvent ofInterestAdded(Long userId, Long stockId, String stockName) {
        return new NotificationEvent(NotificationType.INTEREST_ADDED, userId, stockId, stockName, null);
    }

    /** 뉴스 발생 이벤트 생성 */
    public static NotificationEvent ofNewsEvent(Long stockId, String stockName, String newsTitle) {
        return new NotificationEvent(NotificationType.NEWS_EVENT, null, stockId, stockName, newsTitle);
    }
}
