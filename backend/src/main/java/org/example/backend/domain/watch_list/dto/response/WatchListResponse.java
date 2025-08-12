package org.example.backend.domain.watch_list.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관심 종목 응답 DTO")
public record WatchListResponse(

        @Schema(description = "주식 코드", example = "900110")
        String symbol,

        @Schema(description = "현재 로그인한 사용자가 해당 종목을 관심종목으로 등록했는지 여부 (등록됨: true / 미등록: false)", example = "true")
        boolean watching,

        @Schema(description = "관심 종목 수", example = "1234")
        long watchCount
) {
}
