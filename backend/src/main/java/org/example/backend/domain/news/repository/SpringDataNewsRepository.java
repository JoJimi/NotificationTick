package org.example.backend.domain.news.repository;

import org.example.backend.domain.news.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataNewsRepository extends JpaRepository<News, Long> {
    List<News> findByStockId(Long stockId);
    Optional<News> findTopByStockIdOrderByPublishedAtDesc(Long stockId);
    void deleteByStockId(Long stockId);
    boolean existsByStockIdAndTitleAndPublishedAtAndSource(Long stockId, String title, OffsetDateTime publishedAt, String source);

}
