package com.depth.learningcrew.system.security.utility.redis;

import com.depth.learningcrew.system.security.utility.jwt.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, Object> redisBlackListTemplate;

    @Value("${jwt.refresh-token-expiration-weeks}")
    private long refreshTokenExpirationWeeks;

    /* Refresh Token Redis Utils */
    public void setRefreshToken(String key, String refreshToken) {
        redisTemplate.opsForValue().set(getRefreshKey(key), refreshToken, getRtkExpireSeconds(), TimeUnit.SECONDS);
    }

    public Object getRefreshToken(String key) {
        return redisTemplate.opsForValue().get(getRefreshKey(key));
    }

    public boolean deleteRefreshToken(String key) {
        return redisTemplate.delete(getRefreshKey(key));
    }

    public boolean hasKeyRefreshToken(String key) {
        return redisTemplate.hasKey(getRefreshKey(key));
    }

    public long getRefreshTokenExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(getRefreshKey(key), timeUnit);
    }

    /* Blacklist Token Redis Utils */
    public void setBlackList(String accessToken, long remainingExpiration) {
        redisBlackListTemplate.opsForValue().set(accessToken, "access_token", remainingExpiration, TimeUnit.SECONDS);
    }

    public Object getBlackList(String accessToken) {
        return redisBlackListTemplate.opsForValue().get(accessToken);
    }

    public boolean deleteBlackList(String accessToken) {
        return redisBlackListTemplate.delete(accessToken);
    }

    public boolean hasKeyBlackList(String accessToken) {
        return redisBlackListTemplate.hasKey(accessToken);
    }

    public long getBlacklistExpire(String accessToken, TimeUnit timeUnit) {
        return redisBlackListTemplate.getExpire(accessToken, timeUnit);
    }

    private long getRtkExpireSeconds() {
        return Duration.ofDays(refreshTokenExpirationWeeks * 7).getSeconds();
    }

    private String getRefreshKey(String key) {
        return "refresh_token:" + key;
    }
}
