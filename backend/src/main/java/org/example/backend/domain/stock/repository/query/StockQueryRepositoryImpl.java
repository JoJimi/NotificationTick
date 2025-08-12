package org.example.backend.domain.stock.repository.query;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.stock.dto.response.StockResponse;
import org.example.backend.domain.stock.entity.QStock;
import org.example.backend.domain.watch_list.entity.QWatchList;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StockQueryRepositoryImpl implements StockQueryRepository {

    private final JPAQueryFactory qf;

    private final QStock s = QStock.stock;
    private final QWatchList w = QWatchList.watchList;
    private final QWatchList wUser = new QWatchList("wUser");

    @Override
    public List<StockResponse> findAllOrderByWatchCountDesc() {
        return qf.select(Projections.constructor(
                        StockResponse.class,
                        s.id, s.symbol, s.name, s.market, s.isin,
                        // 🔧 복합키라 단일 id 없음 → 사용자 id 기준으로 카운트
                        w.user.id.count()
                ))
                .from(s)
                .leftJoin(s.watchList, w)
                .groupBy(s.id, s.symbol, s.name, s.market, s.isin)
                .orderBy(w.user.id.count().desc())
                .fetch();
    }

    @Override
    public Optional<StockResponse> findWithWatchCountBySymbol(String symbol) {
        return Optional.ofNullable(
                qf.select(Projections.constructor(
                                StockResponse.class,
                                s.id, s.symbol, s.name, s.market, s.isin,
                                w.user.id.count()
                        ))
                        .from(s)
                        .leftJoin(s.watchList, w)
                        .where(s.symbol.eq(symbol))
                        .groupBy(s.id, s.symbol, s.name, s.market, s.isin)
                        .fetchOne()
        );
    }

    @Override
    public List<StockResponse> findWatchingStocksByUserId(Long userId) {
        return qf.select(Projections.constructor(
                        StockResponse.class,
                        s.id, s.symbol, s.name, s.market, s.isin,
                        w.user.id.count() // 전체 관심수 집계
                ))
                .from(s)
                .join(s.watchList, wUser).on(wUser.user.id.eq(userId)) // 내가 누른 것만
                .leftJoin(s.watchList, w) // 전체 관심수 집계용
                .groupBy(s.id, s.symbol, s.name, s.market, s.isin, wUser.createdAt)
                .orderBy(wUser.createdAt.desc())
                .fetch();
    }
}