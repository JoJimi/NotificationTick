package org.example.shared.common.exception.auth;

import org.example.shared.common.exception.BusinessException;
import org.example.shared.common.exception.ErrorCode;

public class UnAuthorizedException extends BusinessException {
    public UnAuthorizedException() {
        super(ErrorCode.UNAUTHORIZED);
    }
}
