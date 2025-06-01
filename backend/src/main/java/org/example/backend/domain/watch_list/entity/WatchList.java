package org.example.backend.domain.watch_list.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.domain.user.entity.User;
import org.example.backend.global.entity.BaseEntity;

import java.time.OffsetDateTime;

@Entity
@Table(name = "watchlist")
@IdClass(WatchListId.class)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WatchList extends BaseEntity {    /** 관심 종목 **/

    /** 복합키: user_id (NOT NULL) */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 복합키: stock_id (NOT NULL) */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

}
