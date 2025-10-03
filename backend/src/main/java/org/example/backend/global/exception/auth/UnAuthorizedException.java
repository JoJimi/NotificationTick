package org.example.backend.global.exception.auth;

import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;

public class UnAuthorizedException extends BusinessException {
    public UnAuthorizedException() {
        super(ErrorCode.UNAUTHORIZED);
    }
}
