package org.example.backend.domain.watch_list.repository;

import org.example.backend.domain.watch_list.entity.WatchList;

public interface WatchListRepository {
    boolean existsByUserIdAndStockId(Long userId, Long stockId);

    void deleteByUserIdAndStockId(Long userId, Long stockId);

    long countByStockId(Long stockId);

    WatchList save(WatchList watchList);
}
