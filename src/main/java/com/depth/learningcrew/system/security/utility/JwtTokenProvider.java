package com.depth.learningcrew.system.security.utility;

import com.depth.learningcrew.system.security.model.AuthDetails;
import com.depth.learningcrew.system.security.model.JwtDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RequiredArgsConstructor
public class JwtTokenProvider {
    private final Key secret;

    public JwtDto.TokenData createToken(AuthDetails authDetails, Long expiration, TokenType tokenType) {
        Claims claims = Jwts.claims().setSubject(authDetails.getName());

        LocalDateTime expireLocalDateTime;
        switch (tokenType) {
            case ACCESS -> {
                claims.put("tokenType", "ACCESS");
                expireLocalDateTime = LocalDateTime.now().plusMinutes(expiration);
            }
            case REFRESH -> {
                claims.put("tokenType", "REFRESH");
                expireLocalDateTime = LocalDateTime.now().plusWeeks(expiration);
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
