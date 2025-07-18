package com.depth.learningcrew.system.security.exception;

public class JwtBlacklistedTokenException extends JwtAuthenticationException {
    public JwtBlacklistedTokenException() {super("Blacklist 토큰입니다.", 401);}

    public JwtBlacklistedTokenException(String message) {super(message, 401);}
}
