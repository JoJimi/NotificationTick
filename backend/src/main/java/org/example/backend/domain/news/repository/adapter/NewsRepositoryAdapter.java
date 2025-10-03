package org.example.backend.domain.news.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.news.entity.News;
import org.example.backend.domain.news.repository.NewsRepository;
import org.example.backend.domain.news.repository.SpringDataNewsRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NewsRepositoryAdapter implements NewsRepository {

    private final SpringDataNewsRepository repository;

    @Override
    public List<News> findByStockId(Long stockId) {
        return repository.findByStockId(stockId);
    }

    @Override
    public Optional<News> findLatestByStockId(Long stockId) {
        return repository.findTopByStockIdOrderByPublishedAtDesc(stockId);
    }

    @Override
    public News save(News news) {
        return repository.save(news);
    }

    @Override
    public void deleteByStockId(Long stockId) {
        repository.deleteByStockId(stockId);
    }

    @Override
    public boolean existsByStockIdAndTitleAndPublishedAtAndSource(Long stockId, String title, OffsetDateTime publishedAt, String source) {
        return repository.existsByStockIdAndTitleAndPublishedAtAndSource(stockId, title, publishedAt, source);
    }
}
