package org.example.shared.common.exception.portfolio;

import org.example.shared.common.exception.BusinessException;
import org.example.shared.common.exception.ErrorCode;

public class PortfolioNotFoundException extends BusinessException {
    public PortfolioNotFoundException() {
        super(ErrorCode.PORTFOLIO_NOT_FOUND);
    }
}
