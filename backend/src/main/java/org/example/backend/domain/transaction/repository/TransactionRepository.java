package org.example.backend.domain.transaction.repository;

import org.example.backend.domain.portfolio.entity.Portfolio;
import org.example.backend.domain.transaction.entity.Transaction;

import java.util.*;

public interface TransactionRepository {
    List<Transaction> findAllByPortfolio(Portfolio portfolio);
    Optional<Transaction> findByIdAndPortfolio(Long id, Portfolio portfolio);
    Transaction save(Transaction transaction);
    void delete(Transaction transaction);
}
