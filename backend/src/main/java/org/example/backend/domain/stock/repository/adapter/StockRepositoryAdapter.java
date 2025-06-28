package org.example.backend.domain.stock.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.domain.stock.repository.SpringDataStockRepository;
import org.example.backend.domain.stock.repository.StockRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StockRepositoryAdapter implements StockRepository {

    private final SpringDataStockRepository springDataStockRepository;

    @Override
    public Optional<Stock> findBySymbol(String symbol) {
        return springDataStockRepository.findBySymbol(symbol);
    }
}
