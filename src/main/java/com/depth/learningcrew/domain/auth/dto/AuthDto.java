package com.depth.learningcrew.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class AuthDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "토큰 재발행 DTO")
    public static class RecreateRequest {
        @Schema(description = "확인할 ID", example = "dev1234")
        private String id;
        @Schema(description = "재발행할 Refresh Token", example = "refreshTokenString")
        private String refreshToken;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "토큰 정보 DTO")
    public static class TokenInfo {
        @Schema(description = "발급된 Access Token")
        private String accessToken;

        @Schema(description = "발급된 Refresh Token")
        private String refreshToken;

        @Schema(description = "Access Token 만료 시간")
        private LocalDateTime accessTokenExpiresAt;

        @Schema(description = "Refresh Token 만료 시간")
        private LocalDateTime refreshTokenExpiresAt;

        public static TokenInfo of(
                String accessToken,
                String refreshToken,
                LocalDateTime accessTokenExpiresAt,
                LocalDateTime refreshTokenExpiresAt
        ) {
            return TokenInfo.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .accessTokenExpiresAt(accessTokenExpiresAt)
                    .refreshTokenExpiresAt(refreshTokenExpiresAt)
                    .build();
        }
    }
}