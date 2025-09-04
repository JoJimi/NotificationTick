// src/api/notifications.js
import api from './index';

/** 페이지 단위 알림 조회 (최신순) */
export async function fetchNotifications({ page = 1, size = 10 } = {}) {
    const params = {
        page: Math.max(0, Number(page) - 1), // Spring Pageable 0-base
        size: Number(size) || 10,
        sort: 'createdAt,DESC',
    };
    const { data } = await api.get('/api/notifications', { params });
    return data; // Page<NotificationResponse>
}

/** 단건 읽음 처리 */
export async function markNotificationRead(notificationId) {
    const { data } = await api.put(`/api/notifications/${notificationId}/read`);
    return data; // 서버가 최신 상태를 내려주면 활용 가능
}

/** 전체 읽음 처리 */
export async function markAllNotificationsRead() {
    const { data } = await api.put('/api/notifications/read-all');
    return data;
}

