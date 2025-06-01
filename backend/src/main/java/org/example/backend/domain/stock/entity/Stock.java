package org.example.backend.domain.stock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.news.entity.News;
import org.example.backend.domain.prediction.entity.Prediction;
import org.example.backend.domain.transaction.entity.Transaction;
import org.example.backend.domain.watch_list.entity.WatchList;
import org.example.backend.global.entity.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "stock",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_stock_symbol", columnNames = {"symbol"})
        })
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Stock extends BaseEntity {     /** 종목 **/

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 주식 티커 (NOT NULL, UNIQUE) **/
    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    /** 종목 이름 **/
    @Column(name = "name", length = 100)
    private String name;

    /** 거래소/시장 (예: NASDAQ) **/
    @Column(name = "market", length = 50)
    private String market;

    /** 업종 (예: Technology) **/
    @Column(name = "sector", length = 50)
    private String sector;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Transaction> transactions = new HashSet<>();

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Prediction> predictions = new HashSet<>();

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<News> news = new HashSet<>();

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WatchList> watchList = new HashSet<>();

}
