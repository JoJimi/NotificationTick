package org.example.backend.domain.watch_list.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.domain.watch_list.entity.WatchList;
import org.example.backend.domain.watch_list.repository.SpringDataWatchListRepository;
import org.example.backend.domain.watch_list.repository.WatchListRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class WatchListRepositoryAdapter implements WatchListRepository {
    private final SpringDataWatchListRepository repository;

    @Override
    public boolean existsByUserIdAndStockId(Long userId, Long stockId) {
        return repository.existsByUserIdAndStockId(userId, stockId);
    }

    @Override
    public void deleteByUserIdAndStockId(Long userId, Long stockId) {
        repository.deleteByUserIdAndStockId(userId, stockId);
    }

    @Override
    public long countByStockId(Long stockId) {
        return repository.countByStockId(stockId);
    }

    @Override
    public WatchList save(WatchList watchList) {
        return repository.save(watchList);
    }

    @Override
    public List<Stock> findDistinctStockAll() {
        return repository.findDistinctStockAll();
    }
}
