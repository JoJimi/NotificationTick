package org.example.shared.common.exception.stock;

import org.example.shared.common.exception.BusinessException;
import org.example.shared.common.exception.ErrorCode;

public class StockBySymbolNotFoundException extends BusinessException {
    public StockBySymbolNotFoundException() {
        super(ErrorCode.STOCK_BY_SYMBOL_NOT_FOUND);
    }
}
