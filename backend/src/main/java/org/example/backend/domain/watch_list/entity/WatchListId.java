package org.example.backend.domain.watch_list.entity;


import lombok.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * WatchList의 복합키 (user_id, stock_id)를 표현
 */
@NoArgsConstructor
@AllArgsConstructor
public class WatchListId implements Serializable {

    private Long user;
    private Long stock;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WatchListId that = (WatchListId) o;
        return Objects.equals(user, that.user) && Objects.equals(stock, that.stock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, stock);
    }
}
