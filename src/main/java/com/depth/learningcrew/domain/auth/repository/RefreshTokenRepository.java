package com.depth.learningcrew.domain.auth.repository;

import com.depth.learningcrew.system.security.utility.jwt.TokenType;
import com.depth.learningcrew.system.security.utility.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final RedisUtil redisUtil;

    public void setByIdAndRtk(String userKey, String refreshToken) {
        redisUtil.setRefreshToken(userKey, refreshToken);
    }

    public Object getById(String userKey) {
        return redisUtil.getRefreshToken(userKey);
    }

    public void deleteById(String userKey) {
        redisUtil.deleteRefreshToken(userKey);
    }

    public boolean existsById(String userKey) {
        return redisUtil.hasKeyRefreshToken(userKey);
    }

    public long getExpireById(String userKey, TimeUnit timeUnit) {
        return redisUtil.getRefreshTokenExpire(userKey, timeUnit);
    }
}
