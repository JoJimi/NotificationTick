package org.example.backend.domain.watch_list.entity;

import lombok.*;
import java.io.Serializable;

/**
 * WatchList의 복합키 (user_id, stock_id)를 표현
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WatchListId implements Serializable {

    private Long user;
    private Long stock;

}
