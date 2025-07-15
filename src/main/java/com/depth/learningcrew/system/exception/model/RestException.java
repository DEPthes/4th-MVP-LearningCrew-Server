package com.depth.learningcrew.system.exception.model;

import lombok.Getter;

@Getter
public class RestException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String message;

    public RestException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }

    public RestException(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
