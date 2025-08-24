package org.example.backend.domain.stock.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.stock.dto.response.StockResponse;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.domain.stock.repository.SpringDataStockRepository;
import org.example.backend.domain.stock.repository.StockRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class StockRepositoryAdapter implements StockRepository {

    private final SpringDataStockRepository repository;

    @Override
    public Optional<Stock> findBySymbol(String symbol) {
        return repository.findBySymbol(symbol);
    }

    @Override
    public Optional<StockResponse> findWithWatchCountBySymbol(String symbol) {
        return repository.findWithWatchCountBySymbol(symbol);
    }

    @Override
    public Page<StockResponse> findAllWithWatchCountOrderBySymbolAsc(Pageable pageable){
        return repository.findAllWithWatchCountOrderBySymbolAsc(pageable);
    }

    @Override
    public Page<StockResponse> searchWithWatchCountByKeyword(String keyword, Pageable pageable){
        return repository.searchWithWatchCountByKeyword(keyword, pageable);
    }

    @Override
    public Page<StockResponse> findAllOrderByWatchCountDesc(Pageable pageable){
        return repository.findAllOrderByWatchCountDesc(pageable);
    }

    @Override
    public Page<StockResponse> findWatchingStocksByUserId(Long userId, Pageable pageable){
        return repository.findWatchingStocksByUserId(userId, pageable);
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
    public List<String> findAllSymbols() {
        return repository.findAllSymbols();
    }
}
