package com.depth.learningcrew.system.security.exception;

import lombok.Getter;

@Getter
public class JwtAuthenticationException extends RuntimeException {
    private final Integer status;

    public JwtAuthenticationException(String message, Integer status) {
        super(message);
        this.status = status;
    }

    public JwtAuthenticationException(String message, Integer status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

}