package org.example.backend.domain.stock.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.stock.dto.response.StockResponse;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.domain.stock.repository.StockRepository;
import org.example.backend.global.exception.stock.StockBySymbolNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    /** 기본 목록(symbol ASC) */
    public List<StockResponse> getStocksAllOrderBySymbolAsc() {
        return stockRepository.findAllWithWatchCountOrderBySymbolAsc();
    }

    /** 종목 검색 */
    public List<StockResponse> searchStocks(String keyword) {
        return stockRepository.searchWithWatchCountByKeyword(keyword);
    }

    /** 랭킹(관심수 DESC) */
    public List<StockResponse> getStocksRanking() {
        return stockRepository.findAllOrderByWatchCountDesc();
    }

    /** 단일 종목(symbol) 조회 + 관심수 포함 */
    public StockResponse getStockBySymbol(String symbol) {
        return stockRepository.findWithWatchCountBySymbol(symbol)
                .orElseThrow(StockBySymbolNotFoundException::new);
    }

    /** 내가 관심등록한 종목 리스트(최신순) + 각 항목 관심수 포함 */
    public List<StockResponse> getMyWatchStocks(Long userId) {
        return stockRepository.findWatchingStocksByUserId(userId);
    }

}

