package org.example.backend.global.exception.watchList;

import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;

public class WatchingListNotFoundException extends BusinessException {
    public WatchingListNotFoundException() {
        super(ErrorCode.WATCHING_STOCK_NOT_FOUND);
    }
}