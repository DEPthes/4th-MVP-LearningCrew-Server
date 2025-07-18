package com.depth.learningcrew.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.refresh-token-expiration-weeks}")
    private Long refreshTokenExpirationWeeks;

    public void storeRefreshToken(String userKey, String refreshToken) {

        long expirationSeconds = Duration.ofDays(refreshTokenExpirationWeeks*7).getSeconds();
        redisTemplate.opsForValue().set(getRefreshKey(userKey), refreshToken, Duration.ofSeconds(expirationSeconds));
    }

    public Object getRefreshToken(String userKey) {
        return redisTemplate.opsForValue().get(getRefreshKey(userKey));
    }

    public void deleteRefreshToken(String userKey) {
        redisTemplate.delete(getRefreshKey(userKey));
    }

    private String getRefreshKey(String userKey) {
        return "refresh_token:" + userKey;
    }
}
