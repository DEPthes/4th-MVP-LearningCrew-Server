package com.depth.learningcrew.system.security.configurer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.depth.learningcrew.system.security.model.ApiPathPattern;
import lombok.Getter;

@Getter
public class PathPatternConfigurer {
    private final List<ApiPathPattern> includePatternList = new ArrayList<>();
    private final List<ApiPathPattern> excludePatternList = new ArrayList<>();

    public void include(
            String pattern,
            ApiPathPattern.METHODS methods
    ) {
        includePatternList.add(ApiPathPattern.of(pattern, methods));
    }

    public void exclude(
            String pattern,
            ApiPathPattern.METHODS methods
    ) {
        excludePatternList.add(ApiPathPattern.of(pattern, methods));
    }

    public void includeAllOf(Collection<ApiPathPattern> patterns) {
        includePatternList.addAll(patterns);
    }

    public void excludeAllOf(Collection<ApiPathPattern> patterns) {
        excludePatternList.addAll(patterns);
    }

    public void includeAll(ApiPathPattern.METHODS methods) {
        includePatternList.add(ApiPathPattern.of(
                "/**",
                methods
        ));
    }

    public void excludeAll(ApiPathPattern.METHODS methods) {
        excludePatternList.add(ApiPathPattern.of(
                "/**",
                methods
        ));
    }
}