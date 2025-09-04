package org.example.backend.domain.stock.dto.batch;

public record StockUpsert(
        String symbol,
        String name,
        String market,
        String isin,
        Double changeRate,
        Long volume
) {}
