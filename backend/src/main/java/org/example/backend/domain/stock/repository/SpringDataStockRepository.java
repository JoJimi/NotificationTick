package org.example.backend.domain.stock.repository;

import org.example.backend.domain.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SpringDataStockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findBySymbol(String symbol);

    @Query("select s.symbol from Stock s")
    List<String> findAllSymbols();
}
