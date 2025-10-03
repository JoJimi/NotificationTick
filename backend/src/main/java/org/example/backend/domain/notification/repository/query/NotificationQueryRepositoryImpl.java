package org.example.backend.domain.notification.repository.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.backend.domain.notification.entity.Notification;
import org.example.backend.domain.notification.entity.QNotification;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepositoryImpl implements NotificationQueryRepository {

    private final JPAQueryFactory qf;
    private final QNotification n = QNotification.notification;

    @Override
    public Page<Notification> findPageByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable) {
        List<Notification> content = qf.selectFrom(n)
                .where(n.user.id.eq(userId))
                .orderBy(n.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = qf.select(n.count())
                .from(n)
                .where(n.user.id.eq(userId))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public long countUnreadByUserId(Long userId) {
        Long cnt = qf.select(n.count())
                .from(n)
                .where(n.user.id.eq(userId).and(n.isRead.isFalse()))
                .fetchOne();
        return cnt == null ? 0 : cnt;
    }

    @Override
    public int markAsRead(Long id, Long userId) {
        long updated = qf.update(n)
                .set(n.isRead, true)
                .where(n.id.eq(id).and(n.user.id.eq(userId)).and(n.isRead.isFalse()))
                .execute();
        return (int) updated;
    }

    @Override
    public int markAllAsReadByUserId(Long userId) {
        long updated = qf.update(n)
                .set(n.isRead, true)
                .where(n.user.id.eq(userId).and(n.isRead.isFalse()))
                .execute();
        return (int) updated;
    }
}
