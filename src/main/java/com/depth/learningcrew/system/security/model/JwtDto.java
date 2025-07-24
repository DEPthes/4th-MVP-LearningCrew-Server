package com.depth.learningcrew.system.security.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

public class JwtDto {
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    public static class TokenData {
        private String tokenString;
        private LocalDateTime expireAt;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    public static class TokenPair {
        JwtDto.TokenData refreshToken;
        JwtDto.TokenData accessToken;

        public static TokenPair of(JwtDto.TokenData refreshToken, JwtDto.TokenData accessToken) {
            return TokenPair.builder()
                    .refreshToken(refreshToken)
                    .accessToken(accessToken)
                    .build();
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    public static class ParsedTokenData {
        private LocalDateTime expireAt;
        private String subject;
        private String refreshUuid;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TokenInfo {
        @Schema(description = "Access Token", example = "accessTokenContent")
        private String accessToken;
        @Schema(description = "Refresh Token", example = "refreshTokenContent")
        private String refreshToken;
        @Schema(description = "Access Token 만료 시간", example = "ISO DateTime")
        private LocalDateTime accessTokenExpiresAt;
        @Schema(description = "Refresh Token 만료 시간", example = "ISO DateTime")
        private LocalDateTime refreshTokenExpiresAt;

        public static TokenInfo of(JwtDto.TokenPair tokenPair) {
            return TokenInfo.builder()
                    .accessToken(tokenPair.getAccessToken().getTokenString())
                    .refreshToken(tokenPair.getRefreshToken().getTokenString())
                    .accessTokenExpiresAt(tokenPair.getAccessToken().getExpireAt())
                    .refreshTokenExpiresAt(tokenPair.getRefreshToken().getExpireAt())
                    .build();
        }
    }
}
