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
    private int accessTokenExpirationMinutes;

    @Value("${jwt.refresh-token-expiration-weeks}")
    private int refreshTokenExpirationWeeks;

    public JwtDto.TokenData createRefreshToken(AuthDetails authDetails) {
        Claims claims = Jwts.claims().setSubject(authDetails.getName());
        claims.put("tokenType", "REFRESH");

        LocalDateTime expireLocalDateTime = LocalDateTime.now().plusWeeks(refreshTokenExpirationWeeks);

        String tokenString = getTokenString(claims, expireLocalDateTime);

        return JwtDto.TokenData.builder()
                .tokenString(tokenString)
                .expireAt(expireLocalDateTime)
                .build();
    }

    public JwtDto.TokenData createAccessToken(AuthDetails authDetails, String refreshUuid) {
        Claims claims = Jwts.claims().setSubject(authDetails.getName());
        claims.put("tokenType", "ACCESS");
        claims.put("refreshUuid", refreshUuid);

        LocalDateTime expireLocalDateTime = LocalDateTime.now().plusMinutes(accessTokenExpirationMinutes);

        String tokenString = getTokenString(claims, expireLocalDateTime);

        return JwtDto.TokenData.builder()
                .tokenString(tokenString)
                .expireAt(expireLocalDateTime)
                .build();
    }

    public JwtDto.TokenPair createTokenPair(AuthDetails authDetails) {
        JwtDto.TokenData refreshTokenData = createRefreshToken(authDetails);
        JwtDto.TokenData accessTokenData = createAccessToken(authDetails, refreshTokenData.getTokenString());

        return JwtDto.TokenPair.of(refreshTokenData, accessTokenData);
    }

    private String getTokenString(Claims claims, LocalDateTime expireLocalDateTime) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(expireLocalDateTime.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(secret)
                .compact();
    }
}