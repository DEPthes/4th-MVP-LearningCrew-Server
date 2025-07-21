package com.depth.learningcrew.domain.auth.repository;

import com.depth.learningcrew.system.security.utility.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenRepository {

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
