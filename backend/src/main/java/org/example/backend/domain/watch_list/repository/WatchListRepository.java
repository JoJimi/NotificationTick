package org.example.backend.domain.watch_list.repository;

import org.example.backend.domain.watch_list.entity.WatchList;
import org.example.backend.domain.watch_list.entity.WatchListId;

import java.util.List;
import java.util.Optional;

public interface WatchListRepository {
    boolean existsByUserIdAndStockId(Long userId, Long stockId);

    void deleteByUserIdAndStockId(Long userId, Long stockId);

    long countByStockId(Long stockId);

    List<WatchList> findByUserId(Long userId);

    List<WatchList> findByUserIdOrderByCreatedAtDesc(Long userId);

    WatchList save(WatchList watchList);
}
