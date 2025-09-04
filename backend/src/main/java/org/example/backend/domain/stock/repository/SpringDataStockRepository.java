package org.example.backend.domain.stock.repository;

import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.domain.stock.repository.query.StockQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface SpringDataStockRepository
        extends JpaRepository<Stock, Long>, StockQueryRepository {

    Optional<Stock> findBySymbol(String symbol);

    @Query("select s.symbol from Stock s")
    List<String> findAllSymbols();

    List<Stock> findBySymbolIn(Collection<String> symbols);

    @Query("select s from Stock s where abs(s.changeRate) >= :threshold")
    List<Stock> findByAbsChangeRateGte(double threshold);

    @Query("select s from Stock s where s.changeRate >= :threshold")
    List<Stock> findByChangeRateGte(double threshold);

    @Query("select s from Stock s where s.changeRate <= :threshold")
    List<Stock> findByChangeRateLte(double threshold);

}
