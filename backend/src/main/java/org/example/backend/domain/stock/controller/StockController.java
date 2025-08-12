package org.example.backend.domain.stock.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.stock.dto.response.StockResponse;
import org.example.backend.domain.stock.service.StockService;
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
    public ResponseEntity<List<StockResponse>> list(
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        List<StockResponse> responses =
                (keyword == null || keyword.isBlank())
                        ? stockService.getStocksAllOrderBySymbolAsc()
                        : stockService.searchStocks(keyword);
        return ResponseEntity.ok(responses);
    }

    /** 랭킹(관심수 DESC): GET /api/stock/ranking */
    @GetMapping("/ranking")
    public ResponseEntity<List<StockResponse>> ranking() {
        return ResponseEntity.ok(stockService.getStocksRanking());
    }

    /** 단건 조회: GET /api/stock/{symbol} */
    @GetMapping("/{symbol}")
    public ResponseEntity<StockResponse> getStock(@PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getStockBySymbol(symbol));
    }
}
