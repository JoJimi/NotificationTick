package org.example.backend.domain.watch_list.repository;

import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.domain.watch_list.entity.WatchList;

import java.util.*;

public interface WatchListRepository {
    boolean existsByUserIdAndStockId(Long userId, Long stockId);

    void deleteByUserIdAndStockId(Long userId, Long stockId);

    long countByStockId(Long stockId);

    List<WatchList> findByUserId(Long userId);

    List<WatchList> findByUserIdOrderByCreatedAtDesc(Long userId);

    WatchList save(WatchList watchList);

    List<Stock> findDistinctStockAll();

    List<WatchList> findAll();
}
