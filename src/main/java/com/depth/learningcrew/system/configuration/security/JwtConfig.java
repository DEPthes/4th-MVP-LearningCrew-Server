package com.depth.learningcrew.system.configuration.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.security.Key;

@Slf4j
@Configuration
public class JwtConfig {
    private final Key secret;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public JwtConfig(
            HandlerExceptionResolver handlerExceptionResolver,
            @Value("${jwt.secret:#{null}}")
            String secretText
    ) {
        if(StringUtils.hasText(secretText) && secretText.length() < 32) {
            throw new IllegalStateException("Jwt Secret 은 32자 이상이어야 합니다.");
        }else {
            if(StringUtils.hasText(secretText)) {
                secret = Keys.hmacShaKeyFor(secretText.getBytes());
            }else{
                this.secret = Keys.secretKeyFor(SignatureAlgorithm.HS256);
                log.warn("JWT Secret이 설정되지 않았습니다. 랜덤한 값이 생성되어 사용됩니다.");
            }
        }

        this.handlerExceptionResolver = handlerExceptionResolver;
    }
}
