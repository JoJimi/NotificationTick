package org.example.backend.global.exception.portfolio;

import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;

public class PortfolioNotFoundException extends BusinessException {
    public PortfolioNotFoundException() {
        super(ErrorCode.PORTFOLIO_NOT_FOUND);
    }
}
