package org.example.backend.domain.watch_list.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.news.service.NewsService;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.domain.stock.repository.StockRepository;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.watch_list.dto.response.WatchListResponse;
import org.example.backend.domain.watch_list.entity.WatchList;
import org.example.backend.domain.watch_list.repository.WatchListRepository;
import org.example.backend.global.exception.stock.StockBySymbolNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WatchListService {

    private final WatchListRepository watchListRepository;
    private final StockRepository stockRepository;
    private final NewsService newsService;

    /** 관심 등록 (idempotent) */
    @Transactional
    public WatchListResponse addWatch(Long userId, String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(StockBySymbolNotFoundException::new);
        Long stockId = stock.getId();

        if (!watchListRepository.existsByUserIdAndStockId(userId, stockId)) {
            try {
                WatchList watchList = WatchList.builder()
                        .user(User.builder().id(userId).build())
                        .stock(stock)
                        .build();
                watchListRepository.save(watchList);
            } catch (DataIntegrityViolationException e) { /* 중복 추가 예외 무시 */ }
            // ★ 신규 관심종목 뉴스 수집
            newsService.addWatchAndFetchNews(userId, symbol);
        }
        long count = watchListRepository.countByStockId(stockId);
        return new WatchListResponse(symbol, true, count);
    }

    /** 관심 해제 (idempotent) */
    @Transactional
    public WatchListResponse removeWatch(Long userId, String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(StockBySymbolNotFoundException::new);
        Long stockId = stock.getId();

        if (watchListRepository.existsByUserIdAndStockId(userId, stockId)) {
            watchListRepository.deleteByUserIdAndStockId(userId, stockId);
            // ★ 관심종목 해제 시 뉴스 삭제
            newsService.removeWatchAndDeleteNews(userId, symbol);
        }
        long count = watchListRepository.countByStockId(stockId);
        return new WatchListResponse(symbol, false, count);
    }

    /** 토글 (관심 등록/해제) */
    @Transactional
    public WatchListResponse toggleWatch(Long userId, String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(StockBySymbolNotFoundException::new);
        Long stockId = stock.getId();
        boolean exists = watchListRepository.existsByUserIdAndStockId(userId, stockId);

        if (exists) {
            // 관심 -> 해제
            watchListRepository.deleteByUserIdAndStockId(userId, stockId);
            // ★ 뉴스 삭제
            newsService.removeWatchAndDeleteNews(userId, symbol);
        } else {
            // 미관심 -> 관심 등록
            try {
                WatchList watchList = WatchList.builder()
                        .user(User.builder().id(userId).build())
                        .stock(stock)
                        .build();
                watchListRepository.save(watchList);
            } catch (DataIntegrityViolationException e) { /* 중복 예외 무시 */ }
            // ★ 뉴스 수집 (최신 5건)
            newsService.addWatchAndFetchNews(userId, symbol);
        }
        long count = watchListRepository.countByStockId(stockId);
        return new WatchListResponse(symbol, !exists, count);
    }

    /** 단건 상태 확인 */
    @Transactional(readOnly = true)
    public WatchListResponse getWatchState(Long userId, String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(StockBySymbolNotFoundException::new);
        Long stockId = stock.getId();

        boolean watching = watchListRepository.existsByUserIdAndStockId(userId, stockId);
        long count = watchListRepository.countByStockId(stockId);

        return new WatchListResponse(symbol, watching, count);
    }
}
