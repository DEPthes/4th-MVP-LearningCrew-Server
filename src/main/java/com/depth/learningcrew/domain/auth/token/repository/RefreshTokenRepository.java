package com.depth.learningcrew.domain.auth.token.repository;

import com.depth.learningcrew.domain.auth.token.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByUuid(String uuid);
    Optional<RefreshToken> deleteByUuid(String uuid);
    void deleteByUserKey(String userKey);
}
