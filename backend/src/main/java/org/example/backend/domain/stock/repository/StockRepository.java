package org.example.backend.domain.stock.repository;

import org.example.backend.domain.stock.entity.Stock;

import java.util.Optional;

public interface StockRepository {
    Optional<Stock> findBySymbol(String symbol);

}
