package org.example.backend.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.backend.type.AudienceType;
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
        String newsTitle,

        @Schema(description = "등락률", example = "5.2")
        Double changeRate,

        @Schema(description = "그룹 유형", example = "PERSONAL")
        AudienceType audienceType
) {
    /** 관심종목 등록 이벤트 생성 */
    public static NotificationEvent ofInterestAdded(Long userId, Long stockId, String stockName) {
        return new NotificationEvent(NotificationType.INTEREST_ADDED, userId, stockId, stockName, null, null, AudienceType.PERSONAL);
    }

    /** 뉴스 발생 이벤트 생성 */
    public static NotificationEvent ofNewsEvent(Long stockId, String stockName, String newsTitle) {
        return new NotificationEvent(NotificationType.NEWS_EVENT, null, stockId, stockName, newsTitle, null, AudienceType.MULTICAST);
    }

    /** 관심 등록 시 관심종목 급등 알림 이벤트 생성 */
    public static NotificationEvent ofPriceSurgeByInterestAddedEvent(Long userId, Long stockId, String stockName, Double changeRate) {
        return new NotificationEvent(NotificationType.PRICE_SURGE, userId, stockId, stockName, null, changeRate, AudienceType.PERSONAL);
    }

    /** 관심 등록 시 관심종목 급락 알림 이벤트 생성 */
    public static NotificationEvent ofPriceDropByInterestAddedEvent(Long userId, Long stockId, String stockName, Double changeRate) {
        return new NotificationEvent(NotificationType.PRICE_DROP, userId, stockId, stockName, null, changeRate, AudienceType.PERSONAL);
    }

    /** 특정 시간에 관심종목 급등 알림 이벤트 생성 */
    public static NotificationEvent ofPriceSurgeByScheduledEvent(Long stockId, String stockName, Double changeRate) {
        return new NotificationEvent(NotificationType.PRICE_SURGE, null, stockId, stockName, null, changeRate, AudienceType.MULTICAST);
    }

    /** 특정 시간에 관심종목 급락 알림 이벤트 생성 */
    public static NotificationEvent ofPriceDropByScheduledEvent(Long stockId, String stockName, Double changeRate) {
        return new NotificationEvent(NotificationType.PRICE_DROP, null, stockId, stockName, null, changeRate, AudienceType.MULTICAST);
    }
}
