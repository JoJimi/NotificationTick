package org.example.backend.domain.news.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.domain.news.entity.News;
import org.example.backend.domain.news.repository.NewsRepository;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.domain.stock.repository.StockRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import com.fasterxml.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final StockRepository stockRepository;
    private final NewsRepository newsRepository;

    @Value("${naver.api.client-id}")
    private String naverClientId;
    @Value("${naver.api.client-secret}")
    private String naverClientSecret;
    @Value("${naver.api.clova-key}")
    private String clovaApiKey;

    // 네이버 오픈 API 전용
    private final WebClient naverClient = WebClient.builder()
            .baseUrl("https://openapi.naver.com")
            .build();

    // CLOVA Summarization 전용
    private final WebClient clovaClient = WebClient.builder()
            .baseUrl("https://clovastudio.stream.ntruss.com")
            .build();

    // 기사 HTML 크롤링 전용 (인코딩 비간섭)
    private final WebClient articleClient = buildArticleClient();

    private static WebClient buildArticleClient() {
        DefaultUriBuilderFactory f = new DefaultUriBuilderFactory();
        f.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        return WebClient.builder().uriBuilderFactory(f).build();
    }

    // === 날짜 포맷 (RFC1123) ===
    private static final DateTimeFormatter RFC1123_EN =
            DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.ENGLISH);

    private static final DateTimeFormatter RFC1123_EN_ALT1 =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("EEE, dd MMM yyyy HH:mm:ss Z")
                    .toFormatter(Locale.ENGLISH);

    private static final DateTimeFormatter RFC1123_EN_ALT2 =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("EEE, dd MMM yyyy HH:mm Z")
                    .toFormatter(Locale.ENGLISH);

    /** 특정 종목의 최신 뉴스(최대 5개)를 가져와 저장합니다. */
    public List<News> fetchLatestNewsForStock(Stock stock, OffsetDateTime lastPublishedAfter) {
        String query = stock.getName();
        int maxResults = 5;

        String uri = "/v1/search/news.json?query=" + urlEncode(query)
                + "&display=" + maxResults
                + "&start=1&sort=date";

        JsonNode jsonResponse = naverClient.get()
                .uri(uri)
                .header("X-Naver-Client-Id", naverClientId)
                .header("X-Naver-Client-Secret", naverClientSecret)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        List<News> savedNewsList = new ArrayList<>();
        if (jsonResponse == null || !jsonResponse.has("items")) {
            return savedNewsList;
        }

        for (JsonNode item : jsonResponse.get("items")) {
            String title = stripTags(item.path("title").asText(""));
            String link = item.hasNonNull("originallink") && !item.get("originallink").asText("").isEmpty()
                    ? item.get("originallink").asText()
                    : item.path("link").asText("");
            String description = stripTags(item.path("description").asText(""));
            String pubDateStr = item.path("pubDate").asText(null);
            OffsetDateTime publishedAt = parsePubDate(pubDateStr);

            if (publishedAt == null) continue;
            if (lastPublishedAfter != null && (publishedAt.isBefore(lastPublishedAfter) || publishedAt.isEqual(lastPublishedAfter))) continue;
            if (link.isEmpty() || isNewsLinkAlreadySaved(stock.getId(), link)) continue;

            // 기사 본문 크롤링 (재인코딩 방지)
            String contentText = fetchArticleText(link);

            // 요약 시도 (본문 없거나 실패 시 description으로 폴백)
            String summaryText = "";
            if (!contentText.isBlank()) {
                summaryText = summarize(contentText);
            }
            if (summaryText.isBlank()) {
                summaryText = description;
            }

            String source = extractSourceFromUrl(link);

            News news = News.builder()
                    .stock(stock)
                    .title(title)
                    .content(link)
                    .summary(summaryText)
                    .source(source)
                    .publishedAt(publishedAt)
                    .build();

            try {
                newsRepository.save(news);
                savedNewsList.add(news);
            } catch (Exception ignore) { }
        }

        return savedNewsList;
    }

    /** 관심종목 추가 시 최신 뉴스 가져오기 */
    public List<News> addWatchAndFetchNews(Long userId, String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("종목을 찾을 수 없습니다: " + symbol));

        OffsetDateTime lastTime = newsRepository.findLatestByStockId(stock.getId())
                .map(News::getPublishedAt)
                .orElse(null);

        return fetchLatestNewsForStock(stock, lastTime);
    }

    /** 관심종목 해제 시 해당 종목 뉴스 일괄 삭제 */
    public void removeWatchAndDeleteNews(Long userId, String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("종목을 찾을 수 없습니다: " + symbol));
        newsRepository.deleteByStockId(stock.getId());
    }


    /** 기사 본문 텍스트 fetch (Jsoup 사용 또는 WebClient 대체 가능) */
    private String fetchArticleText(String link) {
        try {
            Document doc = Jsoup.connect(link)
                    .userAgent("Mozilla/5.0")
                    .timeout(8000)
                    .ignoreContentType(true)
                    .get();

            String text = doc.text();
            return truncate(text, 8000);
        } catch (Exception e) {
            try {
                String html = articleClient.get()
                        .uri(link) // EncodingMode.NONE으로 재인코딩 안 함
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                if (html == null || html.isEmpty()) {
                    return "";
                }

                String text = Jsoup.parse(html).text();

                return truncate(text, 8000);
            } catch (Exception ignored) {
                return "";
            }
        }
    }

    /** CLOVA Summarization 호출 (실패 시 빈 문자열 반환) */
    private String summarize(String contentText) {
        try {
            String payloadText = truncate(contentText, 8000);

            Map<String, Object> body = Map.of(
                    "texts", List.of(payloadText)
            );

            JsonNode res = clovaClient.post()
                    .uri("/v1/api-tools/summarization/v2")
                    .header("Authorization", "Bearer " + clovaApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (res != null && res.has("result") && res.get("result").has("text")) {
                return res.get("result").get("text").asText("");
            }
        } catch (Exception ignored) { }

        return "";
    }

    /** pubDate 문자열을 OffsetDateTime으로 파싱 (RFC1123 + 보조 포맷) */
    private OffsetDateTime parsePubDate(String pubDateStr) {
        if (pubDateStr == null || pubDateStr.isBlank()) return null;
        try {
            return ZonedDateTime.parse(pubDateStr, RFC1123_EN).toOffsetDateTime();
        } catch (Exception ignored) { }
        try {
            return ZonedDateTime.parse(pubDateStr, RFC1123_EN_ALT1).toOffsetDateTime();
        } catch (Exception ignored) { }
        try {
            return ZonedDateTime.parse(pubDateStr, RFC1123_EN_ALT2).toOffsetDateTime();
        } catch (Exception ignored) { }

        return null;
    }

    /** URL 인코딩 (쿼리 전용) */
    private String urlEncode(String text) {
        return java.net.URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    /** 태그 제거 */
    private String stripTags(String html) {
        if (html == null) return "";

        return html.replaceAll("<[^>]*>", "");
    }

    /** 링크 중복 여부 */
    private boolean isNewsLinkAlreadySaved(Long stockId, String link) {
        List<News> existingList = newsRepository.findByStockId(stockId);

        return existingList.stream().anyMatch(n -> link.equals(n.getContent()));
    }

    /** host 추출 */
    private String extractSourceFromUrl(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            String host = uri.getHost();
            if (host == null) return null;
            if (host.startsWith("www.")) host = host.substring(4);
            return host;
        } catch (Exception e) {
            return null;
        }
    }

    /** 길이 제한 유틸 */
    private String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;

        return s.substring(0, max);
    }
}
