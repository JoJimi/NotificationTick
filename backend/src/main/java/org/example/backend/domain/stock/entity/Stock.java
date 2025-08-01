package org.example.backend.domain.stock.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.news.entity.News;
import org.example.backend.domain.notification.entity.Notification;
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

    /** 주식 코드 (예: KRX 단축 코드 ("005930")) **/
    @Column(name = "symbol", nullable = false, length = 20, unique = true)
    private String symbol;

    /** 종목 이름 **/
    @Column(name = "name", length = 100)
    private String name;

    /** 거래소/시장 (예: KOSPI, KOSDAQ 등) **/
    @Column(name = "market", length = 50)
    private String market;

    /** ISIN 코드 **/
    @Column(name = "isin", nullable = false, length = 12, unique = true)
    private String isin;

    @Builder.Default
    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Transaction> transactions = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<News> news = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WatchList> watchList = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Notification> notifications = new HashSet<>();

}
