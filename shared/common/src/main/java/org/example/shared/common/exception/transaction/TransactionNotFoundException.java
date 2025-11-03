package org.example.shared.common.exception.transaction;

import org.example.shared.common.exception.BusinessException;
import org.example.shared.common.exception.ErrorCode;

public class TransactionNotFoundException extends BusinessException {
    public TransactionNotFoundException() {
        super(ErrorCode.TRANSACTION_NOT_FOUND);
    }
}
