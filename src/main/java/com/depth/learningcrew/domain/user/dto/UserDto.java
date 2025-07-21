package com.depth.learningcrew.domain.user.dto;

import com.depth.learningcrew.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserDto {
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class UserResponse {
        @Schema(description = "사용자 아이디(이메일 형식)", example = "learnit@mju.ac.kr")
        private String id;
        @Schema(description = "사용자 닉네임", example = "LearnIt팀1")
        private String nickname;
        @Schema(description = "계정 생성 시간")
        private LocalDateTime createdAt;
        @Schema(description = "마지막 정보 수정 시간")
        private LocalDateTime lastModifiedAt;

        public static UserResponse from(User user) {
            return UserResponse.builder()
                    .id(user.getId())
                    .nickname(user.getNickname())
                    .createdAt(user.getCreatedAt())
                    .lastModifiedAt(user.getLastModifiedAt())
                    .build();
        }
    }
}
