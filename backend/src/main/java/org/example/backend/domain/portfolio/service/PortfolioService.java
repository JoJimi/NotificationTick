package org.example.backend.domain.portfolio.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.portfolio.dto.request.PortfolioRequest;
import org.example.backend.domain.portfolio.dto.response.PortfolioDetailsResponse;
import org.example.backend.domain.portfolio.dto.response.PortfolioResponse;
import org.example.backend.domain.portfolio.entity.Portfolio;
import org.example.backend.domain.portfolio.repository.PortfolioRepository;
import org.example.backend.domain.transaction.TransactionsResponse;
import org.example.backend.domain.user.entity.User;
import org.example.backend.global.exception.portfolio.PortfolioNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    /** 내 모든 포트폴리오 조회 */
    public List<PortfolioResponse> findAllPortfolios(User user) {
        return portfolioRepository.findAllByUser(user).stream()
                .map(p -> new PortfolioResponse(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getCreatedAt(),
                        p.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    /** 새 포트폴리오 생성 */
    public PortfolioResponse createPortfolio(User user, PortfolioRequest request) {
        Portfolio portfolio = Portfolio.builder()
                .user(user)
                .name(request.name())
                .description(request.description())
                .build();

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        return new PortfolioResponse(
                savedPortfolio.getId(),
                savedPortfolio.getName(),
                savedPortfolio.getDescription(),
                savedPortfolio.getCreatedAt(),
                savedPortfolio.getUpdatedAt()
        );
    }

    /** 기존 포트폴리오 수정 */
    public PortfolioResponse updatePortfolio(User user, Long id, PortfolioRequest request) {
        Portfolio portfolio = portfolioRepository.findByIdAndUser(id, user)
                .orElseThrow(PortfolioNotFoundException::new);

        portfolio.setName(request.name());
        portfolio.setDescription(request.description());

        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        return new PortfolioResponse(
                updatedPortfolio.getId(),
                updatedPortfolio.getName(),
                updatedPortfolio.getDescription(),
                updatedPortfolio.getCreatedAt(),
                updatedPortfolio.getUpdatedAt()
        );
    }

    /** 포트폴리오 상세 조회 (거래 내역 포함) */
    public PortfolioDetailsResponse findDetailsPortfolio(User user, Long id) {
        Portfolio portfolio = portfolioRepository.findByIdAndUser(id, user)
                .orElseThrow(PortfolioNotFoundException::new);

        List<TransactionsResponse> transactions = portfolio.getTransactions().stream()
                .map(TransactionsResponse::of)
                .collect(Collectors.toList());

        return new PortfolioDetailsResponse(
                portfolio.getId(),
                portfolio.getName(),
                portfolio.getDescription(),
                transactions,
                portfolio.getCreatedAt(),
                portfolio.getUpdatedAt()
        );
    }

    /** 포트폴리오 삭제 */
    public void deletePortfolio(User user, Long id) {
        Portfolio portfolio = portfolioRepository.findByIdAndUser(id, user)
                .orElseThrow(PortfolioNotFoundException::new);
        portfolioRepository.delete(portfolio);
    }

}
