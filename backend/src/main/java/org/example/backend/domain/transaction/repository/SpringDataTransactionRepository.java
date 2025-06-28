package org.example.backend.domain.transaction.repository;

import org.example.backend.domain.portfolio.entity.Portfolio;
import org.example.backend.domain.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataTransactionRepository extends JpaRepository<Transaction, Long> {
    /** 포트폴리오별 거래내역 조회 */
    List<Transaction> findAllByPortfolio(Portfolio portfolio);

    /** 단일 트랜잭션 + 포트폴리오 소유자 검증용 */
    Optional<Transaction> findByIdAndPortfolio(Long id, Portfolio portfolio);

}
