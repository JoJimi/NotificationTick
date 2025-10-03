// src/utils/notifications.js
// 타입 라벨 & 태그 스타일
export function typeLabel(t) {
    return ({
        INTEREST_ADDED: '관심등록',
        NEWS_EVENT: '뉴스',
        PRICE_SURGE: '급등',
        PRICE_DROP: '급락',
    })[t] ?? t;
}

export function tagType(t) {
    return ({
        INTEREST_ADDED: 'info',
        NEWS_EVENT: 'success',
        PRICE_SURGE: 'warning',
        PRICE_DROP: 'danger',
    })[t] ?? 'info';
}

/**
 * 알림에 맞는 라우팅 경로 반환
 * 백엔드에서 NotificationResponse에 stockSymbol/stockId/stockName 중 하나만 실어줘도 UX가 좋아짐.
 */
export function routeByNotification(n) {
    if (!n) return '/notifications';
    const symbol = n.stockSymbol || n.symbol; // 백엔드 필드명에 맞춰 넓게 대응
    if (n.type === 'NEWS_EVENT' && symbol) return `/news/${symbol}`;
    if ((n.type === 'PRICE_SURGE' || n.type === 'PRICE_DROP') && symbol) return `/stocks/${symbol}`;
    return '/notifications';
}
