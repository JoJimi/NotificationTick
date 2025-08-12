package org.example.backend.domain.stock.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.stock.dto.response.StockResponse;
import org.example.backend.domain.stock.service.StockService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /** 기본 목록/검색: GET /api/stock?q= */
    @GetMapping
    public ResponseEntity<Page<StockResponse>> list(
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            Pageable pageable
    ) {
        Page<StockResponse> page = keyword.isBlank()
                ? stockService.getStocksAllOrderBySymbolAsc(pageable)
                : stockService.searchStocks(keyword.trim(), pageable);
        return ResponseEntity.ok(page);
    }

    /** 랭킹(관심수 DESC): GET /api/stock/ranking */
    @GetMapping("/ranking")
    public ResponseEntity<Page<StockResponse>> ranking(Pageable pageable) {
        return ResponseEntity.ok(stockService.getStocksRanking(pageable));
    }

    /** 단건 조회: GET /api/stock/{symbol} */
    @GetMapping("/{symbol}")
    public ResponseEntity<StockResponse> getStock(@PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getStockBySymbol(symbol));
    }
}
