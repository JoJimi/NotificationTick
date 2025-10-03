// src/api/transactions.js
import api from './index';

/** 포트폴리오 내 모든 거래내역 조회 */
export async function fetchTransactions(portfolioId) {
    const { data } = await api.get(`/api/portfolios/${portfolioId}/transactions`);
    return Array.isArray(data) ? data : [];
}

/** 새로운 거래 생성 */
export async function createTransaction(portfolioId, body) {
    // body: { stockSymbol, tradeType: 'BUY'|'SELL', quantity: number, price: number }
    const { data } = await api.post(`/api/portfolios/${portfolioId}/transactions`, body);
    return data;
}

/** 단일 거래 상세 조회 (선택적 사용) */
export async function fetchTransaction(portfolioId, transactionId) {
    const { data } = await api.get(`/api/portfolios/${portfolioId}/transactions/${transactionId}`);
    return data;
}

/** 거래 수정 */
export async function updateTransaction(portfolioId, transactionId, body) {
    // body: { tradeType, quantity, price }
    const { data } = await api.patch(`/api/portfolios/${portfolioId}/transactions/${transactionId}`, body);
    return data;
}

/** 거래 삭제 */
export async function deleteTransaction(portfolioId, transactionId) {
    await api.delete(`/api/portfolios/${portfolioId}/transactions/${transactionId}`);
}
