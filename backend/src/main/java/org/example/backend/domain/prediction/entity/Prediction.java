package org.example.backend.domain.prediction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.prediction_model.entity.PredictionModel;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.global.entity.BaseEntity;

import java.time.OffsetDateTime;

@Entity
@Table(name = "prediction")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Prediction extends BaseEntity {   /** 예측 결과 **/

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 예측 대상 종목 (NOT NULL) **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    /** 사용된 모델 (NOT NULL) **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private PredictionModel model;

    /** 예측 수행 일시 (NOT NULL) **/
    @Column(name = "predicted_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime predictedAt;

    /** 예측 결과 데이터 (JSONB, NOT NULL) **/
    @Column(name = "result", nullable = false, columnDefinition = "jsonb")
    private String result;

    /** 예측 메타데이터 (JSONB) **/
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

}
