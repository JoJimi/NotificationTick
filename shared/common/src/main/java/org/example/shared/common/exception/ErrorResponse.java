package org.example.shared.common.exception;

import lombok.Builder;

public record ErrorResponse(String errorCode, String message) {

    @Builder
    public ErrorResponse {
    }
}
