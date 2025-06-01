package org.example.backend.domain.transaction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.portfolio.entity.Portfolio;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.global.entity.BaseEntity;
import org.example.backend.type.TradeType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "transaction")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Transaction extends BaseEntity {       /** 매수/매도 거래 **/

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 거래가 속한 포트폴리오 (NOT NULL) **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    /** 거래 대상 종목 (NOT NULL) **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    /** 거래 일시 (NOT NULL) **/
    @Column(name = "transaction_date", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime transactionDate;

    /** 거래 유형 (NOT NULL, BUY ot SELL) **/
    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", nullable = false, length = 10)
    private TradeType tradeType;

    /** 거래 수량 (NOT NULL) **/
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /** 거래 단가 (NOT NULL) **/
    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    /** 거래 총액 = quantity × price (NOT NULL) */
    @Column(name = "total_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal totalAmount;

    /** 저장/업데이트 직전에 총액을 자동 계산 */
    @PrePersist
    @PreUpdate
    public void calculateTotalAmount() {
        if (this.price != null && this.quantity != null) {
            this.totalAmount = this.price.multiply(BigDecimal.valueOf(this.quantity));
        }
    }
}
