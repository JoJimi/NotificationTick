// src/api/watchlist.js
import api from './index';

/** 내 관심종목 페이지네이션 목록 (최신순) */
export async function fetchMyWatchList({ page = 1, size = 20 } = {}) {
    const params = {
        page: Math.max(0, Number(page) - 1), // 서버 Pageable 0-base
        size: Number(size) || 20,
    };
    const { data } = await api.get('/api/watchlist/me', { params });
    return data; // Spring Page<StockResponse>
}

/** 단건 상태 조회 (watching, watchCount) */
export async function getWatchState(symbol) {
    const { data } = await api.get(`/api/watchlist/${encodeURIComponent(symbol)}`);
    return data; // WatchListResponse
}

/** 관심 등록 */
export async function addWatch(symbol) {
    const { data } = await api.post(`/api/watchlist/${encodeURIComponent(symbol)}`);
    return data; // WatchListResponse
}

/** 관심 해제 */
export async function removeWatch(symbol) {
    const { data } = await api.delete(`/api/watchlist/${encodeURIComponent(symbol)}`);
    return data; // WatchListResponse
}

/** 토글 (등록/해제) */
export async function toggleWatch(symbol) {
    const { data } = await api.post(`/api/watchlist/${encodeURIComponent(symbol)}/toggle`);
    return data; // WatchListResponse
}

export default { fetchMyWatchList, getWatchState, addWatch, removeWatch, toggleWatch };
