package org.example.backend.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.backend.type.LoginType;
import org.example.backend.type.RoleType;

@Schema(description = "현재 로그인된 사용자 프로필 응답 DTO")
public record UserResponse(

        @Schema(description = "사용자 식별자(ID)", example = "1")
        Long id,

        @Schema(description = "사용자 이메일", example = "user@example.com")
        String email,

        @Schema(description = "사용자 닉네임", example = "홍길동")
        String nickname,

        @Schema(description = "로그인 유형 (GOOGLE, KAKAO 등)", example = "GOOGLE")
        LoginType loginType,

        @Schema(description = "사용자 권한", example = "ROLE_USER")
        RoleType role
) {}
