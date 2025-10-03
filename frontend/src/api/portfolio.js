// src/api/portfolio.js
import api from './index';

/** 내 포트폴리오 전체 조회 */
export async function fetchMyPortfolios() {
    const { data } = await api.get('/api/portfolios');
    return Array.isArray(data) ? data : [];
}

/** 새 포트폴리오 생성 */
export async function createPortfolio(body) {
    // body: { name, description }
    const { data } = await api.post('/api/portfolios', body);
    return data;
}

/** 포트폴리오 일부 수정 (PATCH) */
export async function updatePortfolio(portfolioId, body) {
    const { data } = await api.patch(`/api/portfolios/${portfolioId}`, body);
    return data;
}

/** 단일 포트폴리오 상세 (거래내역 포함) */
export async function fetchPortfolioDetails(portfolioId) {
    const { data } = await api.get(`/api/portfolios/${portfolioId}`);
    return data; // PortfolioDetailsResponse
}

/** 포트폴리오 삭제 */
export async function deletePortfolio(portfolioId) {
    await api.delete(`/api/portfolios/${portfolioId}`);
}
