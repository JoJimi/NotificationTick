package org.example.backend.domain.portfolio.repository;

import org.example.backend.domain.portfolio.entity.Portfolio;
import org.example.backend.domain.user.entity.User;

import java.util.*;

public interface PortfolioRepository {
    List<Portfolio> findAllByUser(User user);
    Optional<Portfolio> findByIdAndUser(Long id, User user);
    Portfolio save(Portfolio portfolio);
    void delete(Portfolio portfolio);
}
