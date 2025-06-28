package org.example.backend.global.exception.transaction;

import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;

public class TransactionNotFoundException extends BusinessException {
    public TransactionNotFoundException() {
        super(ErrorCode.TRANSACTION_NOT_FOUND);
    }
}
