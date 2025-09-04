package org.example.backend.domain.stock.repository.query;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.stock.dto.response.StockResponse;
import org.example.backend.domain.stock.entity.QStock;
import org.example.backend.domain.watch_list.entity.QWatchList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class StockQueryRepositoryImpl implements StockQueryRepository {

    private final JPAQueryFactory qf;
    private final QStock s = QStock.stock;
    private final QWatchList w = QWatchList.watchList;
    private final QWatchList wUser = new QWatchList("wUser");

    /** 랭킹(관심수 DESC) */
    @Override
    public Page<StockResponse> findAllOrderByWatchCountDesc(Pageable pageable) {
        List<StockResponse> content = qf.select(Projections.constructor(
                        StockResponse.class,
                        s.id, s.symbol, s.name, s.market, s.isin,
                        s.changeRate, s.volume,
                        w.user.id.count()
                ))
                .from(s)
                .leftJoin(s.watchList, w)
                .groupBy(s.id, s.symbol, s.name, s.market, s.isin, s.changeRate, s.volume)
                .orderBy(w.user.id.count().desc(), s.symbol.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = qf.select(s.count()).from(s).fetchOne();
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    /** 단건 조회(symbol) + 관심수 포함 */
    @Override
    public Optional<StockResponse> findWithWatchCountBySymbol(String symbol) {
        StockResponse dto = qf.select(Projections.constructor(
                        StockResponse.class,
                        s.id, s.symbol, s.name, s.market, s.isin,
                        s.changeRate, s.volume,
                        w.user.id.count()
                ))
                .from(s)
                .leftJoin(s.watchList, w)
                .where(s.symbol.eq(symbol))
                .groupBy(s.id, s.symbol, s.name, s.market, s.isin, s.changeRate, s.volume)
                .fetchOne();

        return Optional.ofNullable(dto);
    }

    /** 기본 목록: symbol ASC */
    @Override
    public Page<StockResponse> findAllWithWatchCountOrderBySymbolAsc(Pageable pageable) {
        List<StockResponse> content = qf.select(Projections.constructor(
                        StockResponse.class,
                        s.id, s.symbol, s.name, s.market, s.isin,
                        s.changeRate, s.volume,
                        w.user.id.count()
                ))
                .from(s)
                .leftJoin(s.watchList, w)
                .groupBy(s.id, s.symbol, s.name, s.market, s.isin, s.changeRate, s.volume)
                .orderBy(s.symbol.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = qf.select(s.count()).from(s).fetchOne();
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    /** 키워드 검색: symbol/name LIKE, symbol ASC */
    @Override
    public Page<StockResponse> searchWithWatchCountByKeyword(String keyword, Pageable pageable) {
        String k = keyword == null ? "" : keyword.trim();

        List<StockResponse> content = qf.select(Projections.constructor(
                        StockResponse.class,
                        s.id, s.symbol, s.name, s.market, s.isin,
                        s.changeRate, s.volume,
                        w.user.id.count()
                ))
                .from(s)
                .leftJoin(s.watchList, w)
                .where(k.isEmpty() ? null :
                        s.symbol.containsIgnoreCase(k).or(s.name.containsIgnoreCase(k)))
                .groupBy(s.id, s.symbol, s.name, s.market, s.isin, s.changeRate, s.volume)
                .orderBy(s.symbol.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = qf.select(s.count())
                .from(s)
                .where(k.isEmpty() ? null :
                        s.symbol.containsIgnoreCase(k).or(s.name.containsIgnoreCase(k)))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    /** 내가 누른 관심종목 리스트(최신순) + 각 항목 관심수 포함 */
    @Override
    public Page<StockResponse> findWatchingStocksByUserId(Long userId, Pageable pageable) {
        List<StockResponse> content = qf.select(Projections.constructor(
                        StockResponse.class,
                        s.id, s.symbol, s.name, s.market, s.isin,
                        s.changeRate, s.volume,
                        w.user.id.count()
                ))
                .from(s)
                .join(s.watchList, wUser).on(wUser.user.id.eq(userId))
                .leftJoin(s.watchList, w)
                .groupBy(s.id, s.symbol, s.name, s.market, s.isin, s.changeRate, s.volume)
                .orderBy(wUser.createdAt.max().desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = qf.select(wUser.count())
                .from(wUser)
                .where(wUser.user.id.eq(userId))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    /** 등락률 내림차순 (최대 상승률 순) */
    @Override
    public Page<StockResponse> findAllOrderByChangeRateDesc(Pageable pageable) {
        List<StockResponse> content = qf.select(Projections.constructor(
                        StockResponse.class,
                        s.id, s.symbol, s.name, s.market, s.isin,
                        s.changeRate, s.volume,
                        w.user.id.count()
                ))
                .from(s)
                .leftJoin(s.watchList, w)
                .groupBy(s.id, s.symbol, s.name, s.market, s.isin, s.changeRate, s.volume)
                .orderBy(s.changeRate.desc(), s.symbol.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = qf.select(s.count()).from(s).fetchOne();
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    /** 거래량 내림차순 (거래량 많은 순) */
    @Override
    public Page<StockResponse> findAllOrderByVolumeDesc(Pageable pageable) {
        List<StockResponse> content = qf.select(Projections.constructor(
                        StockResponse.class,
                        s.id, s.symbol, s.name, s.market, s.isin,
                        s.changeRate, s.volume,
                        w.user.id.count()
                ))
                .from(s)
                .leftJoin(s.watchList, w)
                .groupBy(s.id, s.symbol, s.name, s.market, s.isin, s.changeRate, s.volume)
                .orderBy(s.volume.desc(), s.symbol.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = qf.select(s.count()).from(s).fetchOne();
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
