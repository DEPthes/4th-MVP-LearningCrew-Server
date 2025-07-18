package com.depth.learningcrew.system.security.utility;

import com.depth.learningcrew.system.security.exception.JwtInvalidTokenException;
import com.depth.learningcrew.system.security.exception.JwtParseException;
import com.depth.learningcrew.system.security.exception.JwtTokenExpiredException;
import com.depth.learningcrew.system.security.model.JwtDto;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.security.Key;
import java.time.ZoneId;
import java.util.Optional;

@RequiredArgsConstructor
public class JwtTokenResolver {
    private final Key secret;

    public Optional<String> parseTokenFromRequest(HttpServletRequest request) {
        Optional<String> bearerToken;
        try {
            bearerToken = Optional.ofNullable(request.getHeader("Authorization"));
        }catch (Exception ignored) {
            bearerToken = Optional.empty();
        }

        return bearerToken
                .filter(token -> token.startsWith("Bearer"))
                .map(token -> token.substring(7));
    }

    public JwtDto.ParsedTokenData resolveTokenFromString(String bearerToken) {
        try {
            var parsed = Jwts.parserBuilder().setSigningKey(this.secret).build().parseClaimsJws(bearerToken);
            String subject = parsed.getBody().getSubject();
            return JwtDto.ParsedTokenData.builder()
                    .subject(subject)
                    .expireAt(parsed.getBody().getExpiration().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                    .build();
        }catch (ExpiredJwtException e) {
            throw new JwtTokenExpiredException(e);
        }catch (SignatureException e) {
            throw new JwtInvalidTokenException(e);
        }catch (Exception e) {
            throw new JwtParseException(e);
        }
    }
}
