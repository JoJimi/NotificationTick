package org.example.backend.domain.stock.repository;

import org.example.backend.domain.stock.dto.response.StockResponse;
import org.example.backend.domain.stock.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface StockRepository {
    Optional<Stock> findById(Long id);
    Optional<Stock> findBySymbol(String symbol);
    List<Stock> findAll();

    Page<StockResponse> findAllOrderByWatchCountDesc(Pageable pageable);
    Optional<StockResponse> findWithWatchCountBySymbol(String symbol);

    Page<StockResponse> findAllWithWatchCountOrderBySymbolAsc(Pageable pageable);
    Page<StockResponse> searchWithWatchCountByKeyword(String keyword, Pageable pageable);
    Page<StockResponse> findWatchingStocksByUserId(Long userId, Pageable pageable);

    Stock save(Stock stock);
    List<Stock> saveAll(List<Stock> lists);
    long count();
    void deleteAll();
    List<String> findAllSymbols();

}
