package org.example.backend.global.jwt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT Access/Refresh Token 응답 DTO")
public record TokenResponse (
        @Schema(
                description = "발급된 Access Token",
                example = "eyJh..."
        )
        String accessToken,
        @Schema(
                description = "발급된 Refresh Token",
                example = "eyJh..."
        )
        String refreshToken
){ }


