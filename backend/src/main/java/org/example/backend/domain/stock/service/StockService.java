package org.example.backend.domain.stock.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.stock.dto.response.StockResponse;
import org.example.backend.domain.stock.repository.StockRepository;
import org.example.backend.global.exception.stock.StockBySymbolNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    /** 기본 목록(symbol ASC) */
    public Page<StockResponse> getStocksAllOrderBySymbolAsc(Pageable pageable) {
        return stockRepository.findAllWithWatchCountOrderBySymbolAsc(pageable);
    }

    /** 종목 검색 */
    public Page<StockResponse> searchStocks(String q, Pageable pageable) {
        return stockRepository.searchWithWatchCountByKeyword(q, pageable);
    }

    /** 랭킹(관심수 DESC) */
    public Page<StockResponse> getStocksRanking(Pageable pageable) {
        return stockRepository.findAllOrderByWatchCountDesc(pageable);
    }

    /** 단일 종목(symbol) 조회 + 관심수 포함 */
    public StockResponse getStockBySymbol(String symbol) {
        return stockRepository.findWithWatchCountBySymbol(symbol)
                .orElseThrow(StockBySymbolNotFoundException::new);
    }

    /** 내가 관심등록한 종목 리스트(최신순) + 각 항목 관심수 포함 */
    public Page<StockResponse> getMyWatchStocks(Long userId, Pageable pageable) {
        return stockRepository.findWatchingStocksByUserId(userId, pageable);
    }

    /** 등락률 랭킹 조회 (등락률 높은 순) **/
    public Page<StockResponse> getStocksByChangeRate(Pageable pageable) {
        return stockRepository.findAllOrderByChangeRateDesc(pageable);
    }

    /** 거래량 랭킹 조회 (거래량 많은 순) **/
    public Page<StockResponse> getStocksByVolume(Pageable pageable) {
        return stockRepository.findAllOrderByVolumeDesc(pageable);
    }

}

