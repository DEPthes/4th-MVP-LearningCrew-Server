package com.depth.learningcrew.system.security.exception;

public class JwtInvalidTokenException extends JwtAuthenticationException {
    public JwtInvalidTokenException() {
        super("Invalid token", 401);
    }

    public JwtInvalidTokenException(Throwable cause) {
        super("Invalid token", 401, cause);
    }
}