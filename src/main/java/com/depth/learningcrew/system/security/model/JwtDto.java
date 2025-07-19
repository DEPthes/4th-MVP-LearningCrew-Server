package com.depth.learningcrew.system.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    public static class ParsedTokenData {
        private LocalDateTime expireAt;
        private String subject;
    }
}
