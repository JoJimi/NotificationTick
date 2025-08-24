package org.example.backend.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.portfolio.entity.Portfolio;
import org.example.backend.domain.portfolio.repository.PortfolioRepository;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.domain.stock.repository.StockRepository;
import org.example.backend.domain.transaction.dto.request.TransactionsCreateRequest;
import org.example.backend.domain.transaction.dto.request.TransactionsUpdateRequest;
import org.example.backend.domain.transaction.dto.response.TransactionsResponse;
import org.example.backend.domain.transaction.entity.Transaction;
import org.example.backend.domain.transaction.repository.TransactionRepository;
import org.example.backend.global.exception.portfolio.PortfolioNotFoundException;
import org.example.backend.domain.user.entity.User;
import org.example.backend.global.exception.stock.StockBySymbolNotFoundException;
import org.example.backend.global.exception.transaction.TransactionNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;

    /** 포트폴리오 소유자 검증 헬퍼 */
    private Portfolio verifyOwnership(User user, Long portfolioId) {
        return portfolioRepository.findByIdAndUser(portfolioId, user)
                .orElseThrow(PortfolioNotFoundException::new);
    }

    /** 전체 거래내역 조회 */
    public List<TransactionsResponse> findAllTransactions(User user, Long portfolioId) {
        Portfolio portfolio = verifyOwnership(user, portfolioId);
        return transactionRepository.findAllByPortfolio(portfolio).stream()
                .map(TransactionsResponse::of)
                .collect(Collectors.toList());
    }

    /** 새로운 거래 생성 */
    public TransactionsResponse createTransaction(User user, Long portfolioId, TransactionsCreateRequest request) {
        Portfolio portfolio = verifyOwnership(user, portfolioId);

        Stock stock = stockRepository.findBySymbol(request.stockSymbol())
                .orElseThrow(StockBySymbolNotFoundException::new);

        Transaction transaction = Transaction.builder()
                .portfolio(portfolio)
                .stock(stock)
                .transactionDate(OffsetDateTime.now())
                .tradeType(request.tradeType())
                .quantity(request.quantity())
                .price(request.price())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return TransactionsResponse.of(saved);
    }

    /** 단일 거래 상세 조회 */
    public TransactionsResponse findOneTransaction(User user, Long portfolioId, Long transactionId) {
        Portfolio portfolio = verifyOwnership(user, portfolioId);
        Transaction transaction = transactionRepository.findByIdAndPortfolio(transactionId, portfolio)
                .orElseThrow(TransactionNotFoundException::new);
        return TransactionsResponse.of(transaction);
    }

    /** 거래 정보 수정 */
    public TransactionsResponse updateTransaction(User user, Long portfolioId, Long transactionId, TransactionsUpdateRequest request) {
        Portfolio portfolio = verifyOwnership(user, portfolioId);
        Transaction transaction = transactionRepository.findByIdAndPortfolio(transactionId, portfolio)
                .orElseThrow(TransactionNotFoundException::new);

        transaction.setTradeType(request.tradeType());
        transaction.setQuantity(request.quantity());
        transaction.setPrice(request.price());

        Transaction updated = transactionRepository.save(transaction);
        return TransactionsResponse.of(updated);
    }

    /** 거래 삭제 */
    public void deleteTransaction(User user, Long portfolioId, Long transactionId) {
        Portfolio portfolio = verifyOwnership(user, portfolioId);
        Transaction transaction = transactionRepository.findByIdAndPortfolio(transactionId, portfolio)
                .orElseThrow(TransactionNotFoundException::new);
        transactionRepository.delete(transaction);
    }
}
