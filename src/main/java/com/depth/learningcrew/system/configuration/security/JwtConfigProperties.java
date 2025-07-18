package com.depth.learningcrew.system.configuration.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter @Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfigProperties {
    private String secret;
    private long accessTokenExpirationMinutes;
    private long refreshTokenExpirationWeeks;
}
