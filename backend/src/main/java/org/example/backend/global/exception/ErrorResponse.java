package org.example.backend.global.exception;

import lombok.Builder;

public record ErrorResponse(String errorCode, String message) {

    @Builder
    public ErrorResponse {
    }
}
