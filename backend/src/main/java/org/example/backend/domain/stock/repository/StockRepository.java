package org.example.backend.domain.stock.repository;

import org.example.backend.domain.stock.dto.response.StockResponse;
import org.example.backend.domain.stock.entity.Stock;

import java.util.List;
import java.util.Optional;

public interface StockRepository {
    Optional<Stock> findById(Long id);
    Optional<Stock> findBySymbol(String symbol);
    List<Stock> findAll();

    List<StockResponse> findAllOrderByWatchCountDesc();
    Optional<StockResponse> findWithWatchCountBySymbol(String symbol);

    List<StockResponse> findAllWithWatchCountOrderBySymbolAsc();
    List<StockResponse> searchWithWatchCountByKeyword(String keyword);
    List<StockResponse> findWatchingStocksByUserId(Long userId);

    Stock save(Stock stock);
    List<Stock> saveAll(List<Stock> lists);
    long count();
    void deleteAll();
    List<String> findAllSymbols();

}
