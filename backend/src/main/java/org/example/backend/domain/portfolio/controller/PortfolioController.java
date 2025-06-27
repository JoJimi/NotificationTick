package org.example.backend.domain.portfolio.controller;


import lombok.RequiredArgsConstructor;
import org.example.backend.domain.portfolio.dto.request.PortfolioRequest;
import org.example.backend.domain.portfolio.dto.response.PortfolioDetailsResponse;
import org.example.backend.domain.portfolio.dto.response.PortfolioResponse;
import org.example.backend.domain.portfolio.service.PortfolioService;
import org.example.backend.domain.user.entity.User;
import org.example.backend.global.jwt.custom.CustomUserDetails;
import org.springframework.cglib.SpringCglibInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/portfolios")
@RestController
public class PortfolioController {

    private final PortfolioService portfolioService;

    /** 내 포트폴리오 전체 조회 */
    @GetMapping
    public ResponseEntity<List<PortfolioResponse>> findAllPortfolios(
            @AuthenticationPrincipal CustomUserDetails principal) {

        User user = principal.getUser();
        List<PortfolioResponse> portfolios = portfolioService.findAllPortfolios(user);
        return ResponseEntity.ok(portfolios);
    }

    /** 새 포트폴리오 생성 */
    @PostMapping
    public ResponseEntity<PortfolioResponse> createPortfolio(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody PortfolioRequest request) {

        User user = principal.getUser();
        PortfolioResponse response = portfolioService.createPortfolio(user, request);
        return ResponseEntity.ok(response);
    }

    /** 포트폴리오 일부 수정 */
    @PatchMapping("/{portfolioId}")
    public ResponseEntity<PortfolioResponse> updatePortfolio(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long portfolioId,
            @RequestBody PortfolioRequest request) {

        User user = principal.getUser();
        PortfolioResponse response = portfolioService.updatePortfolio(user, portfolioId, request);
        return ResponseEntity.ok(response);
    }

    /** 단일 포트폴리오 상세 (거래내역 포함) */
    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioDetailsResponse> findDetailsPortfolio(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long portfolioId) {

        User user = principal.getUser();
        PortfolioDetailsResponse response = portfolioService.findDetailsPortfolio(user, portfolioId);
        return ResponseEntity.ok(response);
    }

    /** 포트폴리오 삭제 */
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<Void> deletePortfolio(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long portfolioId) {

        User user = principal.getUser();
        portfolioService.deletePortfolio(user, portfolioId);
        return ResponseEntity.noContent().build();
    }

}
