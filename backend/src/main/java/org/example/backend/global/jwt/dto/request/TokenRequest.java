package org.example.backend.global.jwt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh Token 으로 새로운 Access/Refresh Token 을 요청하는 DTO")
public record TokenRequest(
    @NotBlank(message = "Refresh Token 이 누락되었습니다.")
    @Schema(
            description = "갱신에 사용할 Refresh Token",
            example = "eyJh..."
    )
    String refreshToken
) { }
