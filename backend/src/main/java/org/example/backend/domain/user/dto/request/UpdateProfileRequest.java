package org.example.backend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "사용자 프로필 닉네임 수정 요청 DTO")
public record UpdateProfileRequest(
    @NotBlank(message = "nickname이 빈 값일 수 없습니다.")
    @Schema(
            description = "새로 설정할 닉네임",
            example = "홍길동"
    )
    String nickname
) {}
