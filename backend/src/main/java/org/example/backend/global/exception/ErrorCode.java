package org.example.backend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 공통
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ECO001", "서버 오류가 발생했습니다."),

    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "EU001", "사용자를 찾을 수 없습니다."),

    // 포트폴리오
    PORTFOLIO_NOT_FOUND(HttpStatus.NOT_FOUND, "EP001", "포트폴리오를 찾을 수 없습니다."),

    // 거래내역
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "ET001", "해당 거래를 찾을 수 없습니다."),

    // 종목
    STOCK_BY_SYMBOL_NOT_FOUND(HttpStatus.NOT_FOUND, "ES001", "심볼에 해당하는 종목을 찾을 수 없습니다."),

    // 관심 종목
    WATCHING_STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "EWS001", "해당 종목은 내 관심 목록에 없습니다."),

    // 인증·인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,     "EAU001", "인증에 실패했습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN,           "EAF001", "권한이 없습니다."),

    INVALID_REFRESH_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED,     "EAU002", "리프레시 토큰 검증 실패했습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
