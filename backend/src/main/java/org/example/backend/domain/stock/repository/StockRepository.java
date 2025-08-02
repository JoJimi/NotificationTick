package org.example.backend.domain.stock.repository;

import org.example.backend.domain.stock.entity.Stock;

import java.util.List;
import java.util.Optional;

public interface StockRepository {
    Optional<Stock> findBySymbol(String symbol);
    List<Stock> findAll();
    Stock save(Stock stock);
    List<Stock> saveAll(List<Stock> lists);
    long count();
    void deleteAll();
    List<String> findAllSymbols();
}
