package org.example.backend.domain.watch_list.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.stock.dto.response.StockResponse;
import org.example.backend.domain.stock.service.StockService;
import org.example.backend.domain.watch_list.dto.response.WatchListResponse;
import org.example.backend.domain.watch_list.service.WatchListService;
import org.example.backend.global.jwt.custom.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchListController {

    private final WatchListService watchListService;
    private final StockService stockService;

    /** 관심 등록 */
    @PostMapping("/{symbol}")
    public ResponseEntity<WatchListResponse> add(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String symbol
    ) {
        Long userId = principal.getUser().getId();
        return ResponseEntity.ok(watchListService.addWatch(userId, symbol));
    }

    /** 관심 해제 */
    @DeleteMapping("/{symbol}")
    public ResponseEntity<WatchListResponse> remove(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String symbol
    ) {
        Long userId = principal.getUser().getId();
        return ResponseEntity.ok(watchListService.removeWatch(userId, symbol));
    }

    /** 토글 */
    @PostMapping("/{symbol}/toggle")
    public ResponseEntity<WatchListResponse> toggle(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String symbol
    ) {
        Long userId = principal.getUser().getId();
        return ResponseEntity.ok(watchListService.toggleWatch(userId, symbol));
    }

    /** 단건 상태 확인 (watching + watchCount) */
    @GetMapping("/{symbol}")
    public ResponseEntity<WatchListResponse> state(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String symbol
    ) {
        Long userId = principal.getUser().getId();
        return ResponseEntity.ok(watchListService.getWatchState(userId, symbol));
    }

    /** 내 관심종목 리스트(최신순) + 각 항목 관심수 */
    @GetMapping("/me")
    public ResponseEntity<Page<StockResponse>> myList(
            @AuthenticationPrincipal CustomUserDetails principal,
            Pageable pageable
    ) {
        Long userId = principal.getUser().getId();
        return ResponseEntity.ok(stockService.getMyWatchStocks(userId, pageable));
    }
}
