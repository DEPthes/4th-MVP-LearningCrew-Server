package com.depth.learningcrew.system.configuration.cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {

    private Map<String, Spec> specs = new HashMap<>();

    @Getter
    @Setter
    public static class Spec {
        private long expirationWeek;
        private long maximumSize;
    }
}
