package com.depth.learningcrew.system.exception.handler;


import com.depth.learningcrew.system.exception.dto.ErrorDto;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler({JwtTokenMissingException.class})
    public ResponseEntity<ErrorDto.ErrorResponse> handleJwtTokenMissingException() {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorDto.ErrorResponse.from(ErrorCode.AUTH_TOKEN_NOT_FOUND));
    }

    @ExceptionHandler({JwtTokenExpiredException.class})
    public ResponseEntity<ErrorDto.ErrorResponse> handleJwtTokenExpiredException() {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorDto.ErrorResponse.from(ErrorCode.AUTH_TOKEN_EXPIRED));
    }

    @ExceptionHandler({JwtAuthenticationException.class})
    public ResponseEntity<ErrorDto.ErrorResponse> handleJwtAuthenticationException() {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorDto.ErrorResponse.from(ErrorCode.AUTH_TOKEN_INVALID));
    }

    @ExceptionHandler({JwtInvalidTokenException.class})
    public ResponseEntity<ErrorDto.ErrorResponse> handleJwtInvalidTokenException() {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorDto.ErrorResponse.from(ErrorCode.AUTH_TOKEN_INVALID));
    }
    @ExceptionHandler({JwtParseException.class})
    public ResponseEntity<ErrorDto.ErrorResponse> handleJJwtParseException() {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorDto.ErrorResponse.from(ErrorCode.AUTH_TOKEN_MALFORMED));
    }

    @ExceptionHandler({HttpMessageConversionException.class})
    public ResponseEntity<ErrorDto.ErrorResponse> handleRestException() {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorDto.ErrorResponse.from(ErrorCode.GLOBAL_BAD_REQUEST));
    }

    @ExceptionHandler({BindException.class})
    public ResponseEntity<ErrorDto.ErrorResponse> handleBindException(BindException exception) {
        log.error("{Bind Exception}: {}", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorDto.ValidationErrorResponse.of(
                        ErrorCode.GLOBAL_BAD_REQUEST,
                        exception.getFieldErrors().stream()
                                .map(fieldError -> ErrorDto.ValidationErrorResponse.FieldError.builder()
                                        .field(fieldError.getField())
                                        .message(fieldError.getDefaultMessage())
                                        .build()
                                )
                                .collect(Collectors.toList())
                ));
    }

    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<ErrorDto.ErrorResponse> handleMethodNotSupportedException() {

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorDto.ErrorResponse.from(ErrorCode.GLOBAL_METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorDto.ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ErrorDto.ValidationErrorResponse.of(
                                ErrorCode.GLOBAL_INVALID_PARAMETER,
                                Collections.singletonList(
                                        ErrorDto.ValidationErrorResponse.FieldError.builder()
                                                .field(exception.getParameterName())
                                                .message(exception.getMessage())
                                                .build()
                                )
                        )
                );
    }

    @ExceptionHandler({RestException.class})
    public ResponseEntity<ErrorDto.ErrorResponse> handleRestException(RestException exception) {
        log.error("{Rest Exception}: {}", exception.getErrorCode().getMessage());

        return ResponseEntity
                .status(exception.getErrorCode().getStatusCode())
                .body(ErrorDto.ErrorResponse.from(exception.getErrorCode()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorDto.ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorDto.ValidationErrorResponse.of(
                        ErrorCode.GLOBAL_BAD_REQUEST,
                        exception.getBindingResult().getFieldErrors().stream()
                                .map(fieldError -> ErrorDto.ValidationErrorResponse.FieldError.builder()
                                        .field(fieldError.getField())
                                        .message(fieldError.getDefaultMessage())
                                        .build()
                                )
                                .collect(Collectors.toList())
                ));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorDto.ErrorResponse> handleException(Exception exception) {
        log.error("{Internal Exception}: {}", exception.getMessage(), exception);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDto.ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
