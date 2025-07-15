package com.depth.learningcrew.system.exception.dto;

import com.depth.learningcrew.system.exception.model.ErrorCode;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

public class ErrorDto {

    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @SuperBuilder
    public static class ValidationErrorResponse extends ErrorResponse {
        private List<FieldError> errors;

        @AllArgsConstructor
        @NoArgsConstructor
        @Data
        @Builder
        public static class FieldError {
            private String field;
            private String message;
        }

        public static ValidationErrorResponse of(ErrorCode errorCode, List<FieldError> errors) {
            return ValidationErrorResponse.builder()
                    .statusCode(errorCode.getStatusCode())
                    .message(errorCode.getMessage())
                    .codeName(errorCode.name())
                    .errors(errors)
                    .build();
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @SuperBuilder
    public static class ErrorResponse {
        private Integer statusCode;
        private String message;
        private String codeName;

        public static ErrorResponse from(ErrorCode errorCode) {
            return ErrorResponse.builder()
                    .statusCode(errorCode.getStatusCode())
                    .message(errorCode.getMessage())
                    .codeName(errorCode.name())
                    .build();
        }

        public static ErrorResponse of(Integer statusCode, String message) {
            return ErrorResponse.builder()
                    .statusCode(statusCode)
                    .message(message)
                    .build();
        }
    }
}
