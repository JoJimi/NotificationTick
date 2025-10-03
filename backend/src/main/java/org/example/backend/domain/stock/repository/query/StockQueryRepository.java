package org.example.backend.domain.stock.repository.query;

import org.example.backend.domain.stock.dto.response.StockResponse;
import org.example.backend.domain.stock.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface StockQueryRepository {

    Optional<StockResponse> findWithWatchCountBySymbol(String symbol);
    Page<StockResponse> findAllWithWatchCountOrderBySymbolAsc(Pageable pageable);
    Page<StockResponse> searchWithWatchCountByKeyword(String keyword, Pageable pageable);
    Page<StockResponse> findAllOrderByWatchCountDesc(Pageable pageable);
    Page<StockResponse> findWatchingStocksByUserId(Long userId, Pageable pageable);
    Page<StockResponse> findAllOrderByChangeRateDesc(Pageable pageable);
    Page<StockResponse> findAllOrderByVolumeDesc(Pageable pageable);

    List<Stock> findByAbsChangeRateGte(double threshold);
    List<Stock> findByChangeRateGte(double threshold);
    List<Stock> findByChangeRateLte(double threshold);

}
