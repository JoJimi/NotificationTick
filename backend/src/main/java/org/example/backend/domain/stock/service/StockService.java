package org.example.backend.domain.stock.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.stock.dto.response.StockResponse;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.domain.stock.repository.StockRepository;
import org.example.backend.global.exception.stock.StockBySymbolNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.example.backend.global.exception.ErrorCode.STOCK_BY_SYMBOL_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    /** 전체 종목 조회 */
    public List<StockResponse> getStocksAll() {
        List<Stock> stocks = stockRepository.findAll();
        return stocks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** 단일 종목(symbol) 조회 */
    public StockResponse getStockBySymbol(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(StockBySymbolNotFoundException::new);
        return toResponse(stock);
    }

    /** 엔티티 → DTO 변환 헬퍼 */
    private StockResponse toResponse(Stock s) {
        return new StockResponse(
                s.getId(),
                s.getSymbol(),
                s.getName(),
                s.getMarket(),
                s.getIsin()
        );
    }
}

