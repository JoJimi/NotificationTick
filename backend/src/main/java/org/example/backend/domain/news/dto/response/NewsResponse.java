package org.example.backend.domain.news.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.backend.domain.news.entity.News;

import java.time.OffsetDateTime;

@Schema(description = "뉴스 응답 DTO")
public record NewsResponse(

        @Schema(description = "뉴스 제목", example = "국내 증시 상승 마감")
        String title,

        @Schema(description = "뉴스 원문 URL", example = "http://news.example.com/...?aid=123")
        String content,

        @Schema(description = "뉴스 요약 (본문 요약 또는 발췌)", example = "국내 증시가 오늘 상승 마감했습니다...")
        String summary,

        @Schema(description = "언론사 또는 출처", example = "yonhapnews.co.kr")
        String source,

        @Schema(description = "뉴스 공개 일시 (발행 시간)")
        OffsetDateTime publishedAt

) {
    public static NewsResponse fromEntity(News news) {
        return new NewsResponse(
                news.getTitle(),
                news.getContent(),
                news.getSummary(),
                news.getSource(),
                news.getPublishedAt()
        );
    }
}
