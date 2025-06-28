package org.example.backend.domain.transaction.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.transaction.dto.request.TransactionsCreateRequest;
import org.example.backend.domain.transaction.dto.request.TransactionsUpdateRequest;
import org.example.backend.domain.transaction.dto.response.TransactionsResponse;
import org.example.backend.domain.transaction.service.TransactionService;
import org.example.backend.global.jwt.custom.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /** 포트폴리오 내 모든 거래내역 조회 */
    @GetMapping("/{portfolioId}/transactions")
    public ResponseEntity<List<TransactionsResponse>> findAllTransactions(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long portfolioId){

        List<TransactionsResponse> transactions =
                transactionService.findAllTransactions(principal.getUser(), portfolioId);
        return ResponseEntity.ok(transactions);
    }

    /** 새로운 거래 기록 생성 */
    @PostMapping("/{portfolioId}/transactions")
    public ResponseEntity<TransactionsResponse> CreateTransaction(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long portfolioId,
            @RequestBody TransactionsCreateRequest request){

        TransactionsResponse response =
                transactionService.createTransaction(principal.getUser(), portfolioId, request);
        return ResponseEntity.ok(response);
    }

    /** 단일 거래 상세 조회 */
    @GetMapping("/{portfolioId}/transactions/{transactionId}")
    public ResponseEntity<TransactionsResponse> findOneTransaction(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long portfolioId,
            @PathVariable Long transactionId){

        TransactionsResponse response =
                transactionService.findOneTransaction(principal.getUser(), portfolioId, transactionId);
        return ResponseEntity.ok(response);
    }

    /** 거래 정보 수정 */
    @PatchMapping("/{portfolioId}/transactions/{transactionId}")
    public ResponseEntity<TransactionsResponse> UpdateTransaction(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long portfolioId,
            @PathVariable Long transactionId,
            @RequestBody TransactionsUpdateRequest request){

        TransactionsResponse response =
                transactionService.updateTransaction(principal.getUser(), portfolioId, transactionId, request);
        return ResponseEntity.ok(response);
    }

    /** 거래 정보 삭제 */
    @DeleteMapping("/{portfolioId}/transactions/{transactionId}")
    public ResponseEntity<Void> DeleteTransaction(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long portfolioId,
            @PathVariable Long transactionId){

        transactionService.deleteTransaction(principal.getUser(), portfolioId, transactionId);
        return ResponseEntity.noContent().build();
    }
}
