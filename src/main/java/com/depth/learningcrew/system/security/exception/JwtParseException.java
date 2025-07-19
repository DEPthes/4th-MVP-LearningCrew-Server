package com.depth.learningcrew.system.security.exception;

public class JwtParseException extends JwtAuthenticationException {
    public JwtParseException() {
        super("Failed to parse token", 401);
    }

    public JwtParseException(Throwable cause) {
        super("Failed to parse token", 401, cause);
    }
}