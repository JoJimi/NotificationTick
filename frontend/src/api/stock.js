// src/api/stock.js
import api from './index';

/** 기본 목록/검색 */
export async function fetchStocks({ keyword = '', page = 1, size = 20 } = {}) {
    const params = {
        keyword: keyword.trim(),
        page: Math.max(0, Number(page) - 1), // 서버는 0-base
        size: Number(size) || 20,
    };
    const { data } = await api.get('/api/stock', { params });
    return data; // Spring Page<StockResponse>
}

/** 랭킹(관심수 DESC) */
export async function fetchStockRanking({ page = 1, size = 20 } = {}) {
    const params = {
        page: Math.max(0, Number(page) - 1),
        size: Number(size) || 20,
    };
    const { data } = await api.get('/api/stock/ranking', { params });
    return data;
}

/** 랭킹(등락률 DESC) */
export async function fetchStockRankingByChangeRate({ page = 1, size = 20 } = {}) {
    const params = {
        page: Math.max(0, Number(page) - 1),
        size: Number(size) || 20,
    };
    const { data } = await api.get('/api/stock/ranking/change-rate', { params });
    return data;
}

/** 랭킹(거래량 DESC) */
export async function fetchStockRankingByVolume({ page = 1, size = 20 } = {}) {
    const params = {
        page: Math.max(0, Number(page) - 1),
        size: Number(size) || 20,
    };
    const { data } = await api.get('/api/stock/ranking/volume', { params });
    return data;
}

/** 단건 조회 */
export async function fetchStockBySymbol(symbol) {
    const { data } = await api.get(`/api/stock/${encodeURIComponent(symbol)}`);
    return data; // StockResponse
}
