package com.depth.learningcrew.domain.auth.token.validator;

import com.depth.learningcrew.domain.auth.token.repository.RefreshTokenCacheRepository;
import com.depth.learningcrew.domain.auth.token.repository.RefreshTokenRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenValidator {
    private final RefreshTokenCacheRepository cacheRepository;
    private final RefreshTokenRepository repository;

    public void validateOrThrow(String userKey, String refreshUuid) {
        if (refreshUuid == null || refreshUuid.isBlank()) {
            log.info("Cache miss => refreshUuid: {}", refreshUuid);
            log.warn("Invalid refreshUuid: null or blank.");
            throw new RestException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        if (!isValid(userKey, refreshUuid)) {
            log.warn("RefreshToken invalid: userKey={}, refreshUuid={}", userKey, refreshUuid);
            throw new RestException(ErrorCode.AUTH_TOKEN_INVALID);
        }
    }

    private boolean isValid(String userKey, String refreshUuid) {
        String cachedUserKey = cacheRepository.getCachedRefreshUuid(refreshUuid);
        if (cachedUserKey != null) return cachedUserKey.equals(userKey);

        return repository.findByUuid(refreshUuid)
                .map(token -> token.getUserKey().equals(userKey))
                .orElse(false);
    }
}
