package org.example.backend.domain.news.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.backend.domain.stock.entity.Stock;
import org.example.backend.global.entity.BaseEntity;

import java.time.OffsetDateTime;

@Entity
@Table(name = "news")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class News extends BaseEntity {     /** 뉴스 요약 **/

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 관련 종목 (NOT NULL) **/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    /** 뉴스 제목 **/
    @Column(name = "title", length = 200)
    private String title;

    /** 뉴스 본문 내용 (TEXT) **/
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /** 뉴스 요약 (JSONB) **/
    @Column(name = "summary", columnDefinition = "jsonb")
    private String summary;

    /** 뉴스 공개 일시 **/
    @Column(name = "published_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime publishedAt;

}
