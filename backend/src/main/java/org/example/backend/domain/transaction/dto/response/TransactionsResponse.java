package org.example.backend.domain.transaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.backend.domain.transaction.entity.Transaction;
import org.example.backend.type.TradeType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "거래 내역 응답 DTO")
public record TransactionsResponse(
        @Schema(description = "거래 식별자(ID)", example = "17")
        Long id,

        @Schema(description = "종목 심볼", example = "AAPL")
        String stockSymbol,

        @Schema(description = "매매 타입 (BUY, SELL)", example = "BUY")
        TradeType tradeType,

        @Schema(description = "수량", example = "10")
        Integer quantity,

        @Schema(description = "체결 가격", example = "150.00")
        BigDecimal price,

        @Schema(description = "총 매매 가격", example = "1500.00")
        BigDecimal totalAmount,

        @Schema(description = "생성 일자", example = "2025-06-25T14:22:05Z")
        LocalDateTime createdAt,

        @Schema(description = "수정 일자", example = "2025-06-25T14:22:05Z")
        LocalDateTime updatedAt
) {
        public static TransactionsResponse of(Transaction transaction) {
                return new TransactionsResponse(
                        transaction.getId(),
                        transaction.getStock().getSymbol(),      // Stock 엔티티의 symbol 필드
                        transaction.getTradeType(),              // Transaction.tradeType (BUY/SELL)
                        transaction.getQuantity(),               // 거래 수량
                        transaction.getPrice(),                  // 체결 단가
                        transaction.getTotalAmount(),            // 총 매매 가격
                        transaction.getCreatedAt(),              // BaseEntity.createdAt
                        transaction.getUpdatedAt());               // BaseEntity.updatedAt
        }
}
