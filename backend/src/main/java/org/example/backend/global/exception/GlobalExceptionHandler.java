package org.example.backend.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();

        log.error("[BusinessException] Error Code: {}, Message: {}", errorCode.getCode(), e.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(errorCode.getCode())
                .message(e.getMessage())
                .build();

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(JwtException e) {

        ErrorCode code = ErrorCode.UNAUTHORIZED;

        log.warn("[JwtException] {}", code.getMessage());

        return new ResponseEntity<>(
                ErrorResponse.builder()
                        .errorCode(code.getCode())
                        .message(code.getMessage())
                        .build(),
                code.getHttpStatus()
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {

        ErrorCode code = ErrorCode.UNAUTHORIZED;

        log.warn("[AuthenticationException] {}", code.getMessage());

        return new ResponseEntity<>(
                ErrorResponse.builder()
                        .errorCode(code.getCode())
                        .message(code.getMessage())
                        .build(),
                code.getHttpStatus()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {

        ErrorCode code = ErrorCode.FORBIDDEN;

        log.warn("[AccessDeniedException] {}", code.getMessage());

        return new ResponseEntity<>(
                ErrorResponse.builder()
                        .errorCode(code.getCode())
                        .message(code.getMessage())
                        .build(),
                code.getHttpStatus()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {

        ErrorCode code = ErrorCode.INTERNAL_SERVER_ERROR;

        log.error("[Unhandled Exception] Message: {}", code.getMessage());

        return new ResponseEntity<>(
                ErrorResponse.builder()
                        .errorCode(code.getCode())
                        .message(code.getMessage())
                        .build(),
                code.getHttpStatus());
    }
}
