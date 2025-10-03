package org.example.backend.global.exception.auth;

import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;

/**
 * 리프레시 토큰 검증 실패 시 던질 커스텀 예외
 */
public class InvalidRefreshTokenException extends BusinessException {
    public InvalidRefreshTokenException() {
        super(ErrorCode.INVALID_REFRESH_TOKEN_EXCEPTION);
    }
}
