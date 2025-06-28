package org.example.backend.domain.portfolio.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포트폴리오 요청 정보 DTO")
public record PortfolioRequest(
        @Schema(description = "포트폴리오 이름", example = "테크장기투자")
        String name,

        @Schema(description = "포트폴리오 설명", example = "AI·클라우드 분야 중심으로 5년 이상 홀딩")
        String description
) {
}
