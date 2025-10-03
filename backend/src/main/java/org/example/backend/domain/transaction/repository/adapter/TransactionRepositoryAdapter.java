package org.example.backend.domain.transaction.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.portfolio.entity.Portfolio;
import org.example.backend.domain.transaction.entity.Transaction;
import org.example.backend.domain.transaction.repository.SpringDataTransactionRepository;
import org.example.backend.domain.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final SpringDataTransactionRepository springDataTransactionRepository;

    @Override
    public List<Transaction> findAllByPortfolio(Portfolio portfolio) {
        return springDataTransactionRepository.findAllByPortfolio(portfolio);
    }

    @Override
    public Optional<Transaction> findByIdAndPortfolio(Long id, Portfolio portfolio) {
        return springDataTransactionRepository.findByIdAndPortfolio(id, portfolio);
    }

    @Override
    public Transaction save(Transaction transaction) {
        return springDataTransactionRepository.save(transaction);
    }

    @Override
    public void delete(Transaction transaction) {
        springDataTransactionRepository.delete(transaction);
    }
}
