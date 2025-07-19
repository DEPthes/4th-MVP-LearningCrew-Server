package com.depth.learningcrew.system.security.utility.jwt;

import com.depth.learningcrew.system.security.model.AuthDetails;
import com.depth.learningcrew.system.security.model.JwtDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RequiredArgsConstructor
public class JwtTokenProvider {
    private final Key secret;

    @Value("${jwt.access-token-expiration-minutes}")
    private Long accessTokenExpirationMinutes;

    @Value("${jwt.refresh-token-expiration-weeks}")
    private Long refreshTokenExpirationWeeks;

    public JwtDto.TokenData createToken(AuthDetails authDetails, TokenType tokenType) {
        Claims claims = Jwts.claims().setSubject(authDetails.getName());

        LocalDateTime expireLocalDateTime;
        switch (tokenType) {
            case ACCESS -> {
                claims.put("tokenType", "ACCESS");
                expireLocalDateTime = LocalDateTime.now().plusMinutes(accessTokenExpirationMinutes);
            }
            case REFRESH -> {
                claims.put("tokenType", "REFRESH");
                expireLocalDateTime = LocalDateTime.now().plusWeeks(refreshTokenExpirationWeeks);
            }
            default -> throw new IllegalArgumentException("Unknown token type: " + tokenType);
        }

        String tokenString = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(expireLocalDateTime.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(secret)
                .compact();

        return JwtDto.TokenData.builder()
                .tokenString(tokenString)
                .expireAt(expireLocalDateTime)
                .build();
    }
}