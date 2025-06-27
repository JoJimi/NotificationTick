package org.example.backend.domain.portfolio.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.portfolio.entity.Portfolio;
import org.example.backend.domain.portfolio.repository.PortfolioRepository;
import org.example.backend.domain.portfolio.repository.SpringDataPortfolioRepository;
import org.example.backend.domain.user.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PortfolioRepositoryAdapter implements PortfolioRepository {

    private final SpringDataPortfolioRepository springDataPortfolioRepository;

    @Override
    public List<Portfolio> findAllByUser(User user) {
        return springDataPortfolioRepository.findAllByUser(user);
    }

    @Override
    public Optional<Portfolio> findByIdAndUser(Long id, User user) {
        return springDataPortfolioRepository.findByIdAndUser(id, user);
    }

    @Override
    public Portfolio save(Portfolio portfolio) {
        return springDataPortfolioRepository.save(portfolio);
    }

    @Override
    public void delete(Portfolio portfolio) {
        springDataPortfolioRepository.delete(portfolio);
    }
}
