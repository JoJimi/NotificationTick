package org.example.backend.domain.stock.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "종목 정보 응답 DTO")
public record StockResponse(

        @Schema(description = "종목 식별자(ID)", example = "1")
        Long id,

        @Schema(description = "주식 코드", example = "900110")
        String symbol,

        @Schema(description = "종목 이름", example = "이스트아시아홀딩스")
        String name,

        @Schema(description = "거래소/시장", example = "KOSDAQ")
        String market,

        @Schema(description = "ISIN 코드", example = "HK0000057197")
        String isin
) { }
