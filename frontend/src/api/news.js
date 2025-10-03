// src/api/news.js
import api from './index';

/** 특정 종목(symbol)의 뉴스 목록 조회 */
export async function fetchNewsBySymbol(symbol) {
    const { data } = await api.get(`/api/news/${encodeURIComponent(symbol)}`);
    return Array.isArray(data) ? data : [];
}
