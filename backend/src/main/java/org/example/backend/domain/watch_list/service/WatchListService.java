package org.example.backend.domain.watch_list.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.domain.stock.repository.StockRepository;
import org.example.backend.domain.user.entity.User;
import org.example.backend.domain.user.service.UserService;
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
    private final UserService userService;

    /** 관심 등록 (idempotent) */
    @Transactional
    public WatchListResponse addWatch(Long userId, String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(StockBySymbolNotFoundException::new);
        Long stockId = stock.getId();

        if (!watchListRepository.existsByUserIdAndStockId(userId, stockId)) {
            try {
                WatchList wl = WatchList.builder()
                        .user(User.builder().id(userId).build())
                        .stock(stock)
                        .build();
                watchListRepository.save(wl);
            } catch (DataIntegrityViolationException e) {
                // 동시성으로 중복 insert 시 DB 유니크 제약에 걸리더라도 무시(idempotent)
            }
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
        }
        long count = watchListRepository.countByStockId(stockId);
        return new WatchListResponse(symbol, false, count);
    }

    /** 토글 */
    @Transactional
    public WatchListResponse toggleWatch(Long userId, String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(StockBySymbolNotFoundException::new);
        Long stockId = stock.getId();

        boolean exists = watchListRepository.existsByUserIdAndStockId(userId, stockId);
        if (exists) {
            watchListRepository.deleteByUserIdAndStockId(userId, stockId);
        } else {
            try {
                WatchList wl = WatchList.builder()
                        .user(User.builder().id(userId).build())
                        .stock(stock)
                        .build();
                watchListRepository.save(wl);
            } catch (DataIntegrityViolationException e) {
                // 경합 시에도 일관되게 처리
            }
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
