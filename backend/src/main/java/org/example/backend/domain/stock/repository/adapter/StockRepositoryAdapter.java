package org.example.backend.domain.stock.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.domain.stock.repository.SpringDataStockRepository;
import org.example.backend.domain.stock.repository.StockRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StockRepositoryAdapter implements StockRepository {

    private final SpringDataStockRepository repository;

    @Override
    public Optional<Stock> findBySymbol(String symbol) {
        return repository.findBySymbol(symbol);
    }

    @Override
    public List<Stock> findAll() {
        return repository.findAll();
    }

    @Override
    public Stock save(Stock stock) {
        return repository.save(stock);
    }

    @Override
    public List<Stock> saveAll(List<Stock> lists) {
        return repository.saveAll(lists);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public List<String> findAllIsins() {
        return repository.findAllIsin();
    }
}
