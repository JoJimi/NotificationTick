package org.example.backend.domain.watch_list.repository;

import org.example.backend.domain.watch_list.entity.WatchList;
import org.example.backend.domain.watch_list.entity.WatchListId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataWatchListRepository extends JpaRepository<WatchList, WatchListId> {

    boolean existsByUserIdAndStockId(Long userId, Long stockId);

    void deleteByUserIdAndStockId(Long userId, Long stockId);

    long countByStockId(Long stockId);

    List<WatchList> findByUserId(Long userId);

    List<WatchList> findByUserIdOrderByCreatedAtDesc(Long userId);

}
