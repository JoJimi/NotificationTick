package org.example.backend.domain.stock.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.stock.dto.response.StockResponse;
import org.example.backend.domain.stock.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /** 전체 종목 조회: GET /api/stock */
    @GetMapping
    public ResponseEntity<List<StockResponse>> getAllStocks() {
        List<StockResponse> responses = stockService.getStocksAll();
        return ResponseEntity.ok(responses);
    }

    /** 단일 종목 조회: GET /api/stock/{symbol} */
    @GetMapping("/{symbol}")
    public ResponseEntity<StockResponse> getStock(
            @PathVariable("symbol") String symbol
    ) {
        StockResponse response = stockService.getStockBySymbol(symbol);
        return ResponseEntity.ok(response);
    }

}
