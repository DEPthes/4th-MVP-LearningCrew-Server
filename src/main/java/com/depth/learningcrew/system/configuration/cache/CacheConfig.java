package com.depth.learningcrew.system.configuration.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

    private final CacheProperties cacheProperties;

    @Bean
    public CacheManager cacheManager() {
        return new CustomCaffeineCacheManager(cacheProperties);
    }
}
