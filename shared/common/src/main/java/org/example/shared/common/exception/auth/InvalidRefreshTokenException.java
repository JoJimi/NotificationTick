package org.example.shared.common.exception.auth;

import org.example.shared.common.exception.BusinessException;
import org.example.shared.common.exception.ErrorCode;

/**
 * 리프레시 토큰 검증 실패 시 던질 커스텀 예외
 */
public class InvalidRefreshTokenException extends BusinessException {
    public InvalidRefreshTokenException() {
        super(ErrorCode.INVALID_REFRESH_TOKEN_EXCEPTION);
    }
}
