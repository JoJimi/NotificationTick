package org.example.backend.global.exception.stock;

import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;

public class StockBySymbolNotFoundException extends BusinessException {
    public StockBySymbolNotFoundException() {
        super(ErrorCode.STOCK_BY_SYMBOL_NOT_FOUND);
    }
}
