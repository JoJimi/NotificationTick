package org.example.shared.common.exception.user;

import org.example.shared.common.exception.BusinessException;
import org.example.shared.common.exception.ErrorCode;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
