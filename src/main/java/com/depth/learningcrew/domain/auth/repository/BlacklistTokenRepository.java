package com.depth.learningcrew.domain.auth.repository;

import com.depth.learningcrew.system.security.utility.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BlacklistTokenRepository {

    @Value("${jwt.access-token-expiration-minutes}")
    private int accessTokenExpirationMinutes;

    private final RedisUtil redisUtil;

    public void add(String  token) {
        redisUtil.setBlackList(token, accessTokenExpirationMinutes);
    }

    public boolean exists(String token) {
        return redisUtil.hasKeyBlackList(token);
    }

    public boolean delete(String token) {
        return redisUtil.deleteBlackList(token);
    }

    public long getTTL(String token) {
        return redisUtil.getExpire(token, null);
    }
}
