package com.depth.learningcrew.system.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiPathPattern {
    private String pattern;
    private METHODS method;

    public enum METHODS {
        GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD;

        public static METHODS parse(String method) {
            try {
                return METHODS.valueOf(method.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public static ApiPathPattern of(String pattern, METHODS method) {
        return ApiPathPattern.builder()
                .pattern(pattern)
                .method(method)
                .build();
    }

    public boolean equals(ApiPathPattern other) {
        if (other == null) return false;
        return this.pattern.equals(other.pattern) && this.method.equals(other.method);
    }
}
