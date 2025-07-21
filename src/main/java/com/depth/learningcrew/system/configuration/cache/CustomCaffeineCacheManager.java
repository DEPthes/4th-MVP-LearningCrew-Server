package com.depth.learningcrew.system.configuration.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class CustomCaffeineCacheManager extends CaffeineCacheManager {

    private final CacheProperties cacheProperties;

    @Override
    protected Cache createCaffeineCache(String cacheKeyName) {
        CacheProperties.Spec spec = cacheProperties.getSpecs().get(cacheKeyName);
        if (spec == null) {
            log.warn("No cache spec found for key '{}', using default settings", cacheKeyName);
            return new CaffeineCache(cacheKeyName,
                    Caffeine.newBuilder()
                            .expireAfterWrite(7, TimeUnit.DAYS)
                            .maximumSize(10000)
                            .build());
        }
        log.info("Creating cache for key '{}': expireAfterWrite={}weeks, maximumSize={}", cacheKeyName, spec.getExpirationWeek(), spec.getMaximumSize());
        return new CaffeineCache(cacheKeyName,
                Caffeine.newBuilder()
                        .expireAfterWrite(spec.getExpirationWeek() * 7, TimeUnit.DAYS)
                        .maximumSize(spec.getMaximumSize())
                        .build());
    }
}
