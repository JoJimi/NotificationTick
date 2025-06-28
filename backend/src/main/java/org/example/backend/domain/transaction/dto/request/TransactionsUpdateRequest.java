package org.example.backend.domain.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.backend.type.TradeType;

import java.math.BigDecimal;

@Schema(description = "거래 내역 수정 요청 DTO")
public record TransactionsUpdateRequest(

        @Schema(description = "매매 타입 (BUY, SELL)", example = "BUY")
        TradeType tradeType,

        @Schema(description = "수량", example = "10")
        Integer quantity,

        @Schema(description = "체결 가격", example = "150.00")
        BigDecimal price

) {
}
