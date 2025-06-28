package org.example.backend.domain.portfolio.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.backend.domain.transaction.dto.response.TransactionsResponse;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "포트폴리오 상세 응답 정보 DTO")
public record PortfolioDetailsResponse(

        @Schema(description = "포트폴리오 식별자(ID)", example = "3")
        Long id,

        @Schema(description = "포트폴리오 이름", example = "테크장기투자")
        String name,

        @Schema(description = "포트폴리오 설명", example = "AI·클라우드 분야 중심으로 5년 이상 홀딩")
        String description,

        @Schema(description = "거래 내역")
        List<TransactionsResponse> transaction,

        @Schema(description = "생성 일자", example = "2025-06-25T14:22:05Z")
        LocalDateTime createdAt,

        @Schema(description = "수정 일자", example = "2025-06-25T14:22:05Z")
        LocalDateTime updatedAt
) {
}
