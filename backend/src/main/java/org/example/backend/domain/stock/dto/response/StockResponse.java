package org.example.backend.domain.stock.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;

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
        String isin,

        @Schema(description = "등락률(%)", example = "5.25")
        Double changeRate,

        @Schema(description = "거래량", example = "1000000")
        Long volume,

        @Schema(description = "관심 종목 수", example = "1234")
        long watchCount
) { }
