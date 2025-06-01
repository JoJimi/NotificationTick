package org.example.backend.domain.prediction_model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.prediction.entity.Prediction;
import org.example.backend.global.entity.BaseEntity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "prediction_model",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_prediction_model_name", columnNames = {"model_name"})
        })
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PredictionModel extends BaseEntity {  /** 예측 모델 메타정보 **/

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 모델 이름 (NOT NULL, UNIQUE) **/
    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    /** 알고리즘 (예: LSTM, RandomForest) **/
    @Column(name = "algorithm", length = 100)
    private String algorithm;

    /** 모델 정확도 (REAL) **/
    @Column(name = "accuracy", columnDefinition = "REAL")
    private BigDecimal accuracy;

    /** 하이퍼파라미터 정보 (JSONB) **/
    @Column(name = "hyper_parameters", columnDefinition = "jsonb")
    private String hyperParameters;

    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Prediction> predictions = new HashSet<>();
}
