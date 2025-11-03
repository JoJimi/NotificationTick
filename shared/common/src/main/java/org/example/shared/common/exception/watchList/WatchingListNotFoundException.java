package org.example.shared.common.exception.watchList;

import org.example.shared.common.exception.BusinessException;
import org.example.shared.common.exception.ErrorCode;

public class WatchingListNotFoundException extends BusinessException {
    public WatchingListNotFoundException() {
        super(ErrorCode.WATCHING_STOCK_NOT_FOUND);
    }
}