package org.example.backend.domain.news.schedule;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.news.entity.News;
import org.example.backend.domain.news.repository.NewsRepository;
import org.example.backend.domain.news.service.NewsService;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.domain.watch_list.repository.WatchListRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NewsScheduler {

    private final WatchListRepository watchListRepository;
    private final NewsRepository newsRepository;
    private final NewsService newsService;

    /**
     * 매일 오전 6시, 오후 6시에 관심종목 뉴스 최신화 작업을 수행한다.
     * 크론 표현식 "0 0 6,18 * * *" : 매일 6:00, 18:00 실행
     */
    @Scheduled(cron = "0 0 6,18 * * *")
    public void refreshWatchedStocksNews() {
        // 관심등록된 모든 종목 목록 조회 (중복 제거)
        List<Stock> watchedStocks = watchListRepository.findDistinctStockAll();
        for (Stock stock : watchedStocks) {
            // 해당 종목의 마지막 뉴스 시간 이후 뉴스만 갱신
            OffsetDateTime lastTime = newsRepository.findLatestByStockId(stock.getId())
                    .map(News::getPublishedAt)
                    .orElse(null);
            newsService.fetchLatestNewsForStock(stock, lastTime);
        }
    }
}
