package org.example.backend.domain.portfolio.repository;

import org.example.backend.domain.portfolio.entity.Portfolio;
import org.example.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface SpringDataPortfolioRepository extends JpaRepository<Portfolio, Long> {

    /**
     * 사용자별 포트폴리오 조회
      */
    List<Portfolio> findAllByUser(User user);

    /**
     * ID + 소유자 기준 포트폴리오 조회
     */
    Optional<Portfolio> findByIdAndUser(Long id, User user);

}
