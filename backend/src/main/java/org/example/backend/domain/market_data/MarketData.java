package org.example.backend.domain.market_data;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.global.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "market_data",
        uniqueConstraints = @UniqueConstraint(name = "uq_marketdata_stock_timestamp", columnNames = {"stock_id", "timestamp"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MarketData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 대상 종목 **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    /** 데이터 기록 시점(예: 일간 종가의 날짜 또는 분봉의 실시간 타임스탬프) **/
    @Column(name = "recorded_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime recordedAt;

    /** 시가 **/
    @Column(name = "open", nullable = false, precision = 15, scale = 2)
    private BigDecimal open;

    /** 종가 **/
    @Column(name = "close", nullable = false, precision = 15, scale = 2)
    private BigDecimal close;

    /** 고가 **/
    @Column(name = "high", precision = 15, scale = 2)
    private BigDecimal high;

    /** 저가 **/
    @Column(name = "low", precision = 15, scale = 2)
    private BigDecimal low;

    /** 거래량 **/
    @Column(name = "volume")
    private Long volume;
}
