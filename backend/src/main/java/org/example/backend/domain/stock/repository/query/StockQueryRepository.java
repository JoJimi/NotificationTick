package org.example.backend.domain.stock.repository.query;

import org.example.backend.domain.stock.dto.response.StockResponse;

import java.util.List;
import java.util.Optional;

public interface StockQueryRepository {

    List<StockResponse> findAllOrderByWatchCountDesc();
    Optional<StockResponse> findWithWatchCountBySymbol(String symbol);
    List<StockResponse> findAllWithWatchCountOrderBySymbolAsc();
    List<StockResponse> searchWithWatchCountByKeyword(String keyword);
    List<StockResponse> findWatchingStocksByUserId(Long userId);
}
