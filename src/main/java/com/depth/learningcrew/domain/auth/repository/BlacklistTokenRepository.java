package com.depth.learningcrew.domain.auth.repository;

import com.depth.learningcrew.system.security.utility.jwt.TokenType;
import com.depth.learningcrew.system.security.utility.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class BlacklistTokenRepository {

    private final RedisUtil redisUtil;

    public void setByAtk(String  token) {
        redisUtil.setBlackList(token, TokenType.ACCESS);
    }

    public Object getByAtk(String token) {
        return redisUtil.getBlackList(token);
    }

    public boolean existsByAtk(String token) {
        return redisUtil.hasKeyBlackList(token);
    }

    public boolean deleteByAtk(String token) {
        return redisUtil.deleteBlackList(token);
    }

    public boolean hasKeyByAtk(String token) {
        return redisUtil.hasKeyBlackList(token);
    }

    public long getExpireByAtk(String token) {
        return redisUtil.getBlacklistExpire(token, TimeUnit.SECONDS);
    }
}
