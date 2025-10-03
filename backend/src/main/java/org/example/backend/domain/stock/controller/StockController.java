package org.example.backend.domain.stock.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.stock.dto.response.StockResponse;
import org.example.backend.domain.stock.service.StockService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /** 기본 목록/검색 */
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

    /** 관심수 랭킹 (관심등록 많은 순) */
    @GetMapping("/ranking")
    public ResponseEntity<Page<StockResponse>> ranking(Pageable pageable) {
        return ResponseEntity.ok(stockService.getStocksRanking(pageable));
    }

    /** 등락률 랭킹 (상승률 높은 순) */
    @GetMapping("/ranking/change-rate")
    public ResponseEntity<Page<StockResponse>> rankingByChangeRate(Pageable pageable) {
        return ResponseEntity.ok(stockService.getStocksByChangeRate(pageable));
    }

    /** 거래량 랭킹 (거래량 많은 순) */
    @GetMapping("/ranking/volume")
    public ResponseEntity<Page<StockResponse>> rankingByVolume(Pageable pageable) {
        return ResponseEntity.ok(stockService.getStocksByVolume(pageable));
    }

    /** 단건 조회 */
    @GetMapping("/{symbol}")
    public ResponseEntity<StockResponse> getStock(@PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getStockBySymbol(symbol));
    }
}
