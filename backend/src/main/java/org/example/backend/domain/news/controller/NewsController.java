package org.example.backend.domain.news.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.news.entity.News;
import org.example.backend.domain.news.repository.NewsRepository;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.domain.stock.repository.StockRepository;
import org.example.backend.domain.news.dto.response.NewsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final StockRepository stockRepository;
    private final NewsRepository newsRepository;

    /** 특정 종목(symbol)의 뉴스 목록 조회 */
    @GetMapping("/{symbol}")
    public ResponseEntity<List<NewsResponse>> getNewsBySymbol(@PathVariable String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("종목을 찾을 수 없습니다."));

        List<News> newsList = newsRepository.findByStockId(stock.getId());
        List<NewsResponse> responseList = newsList.stream()
                .map(NewsResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }
}
