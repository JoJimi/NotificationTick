package org.example.backend.domain.news.repository;

import org.example.backend.domain.news.entity.News;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface NewsRepository {
    List<News> findByStockId(Long stockId);
    Optional<News> findLatestByStockId(Long stockId);
    News save(News news);
    void deleteByStockId(Long stockId);
    boolean existsByStockIdAndTitleAndPublishedAtAndSource(Long stockId, String title, OffsetDateTime publishedAt, String source);
}
