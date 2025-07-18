package com.depth.learningcrew.system.security.configurer;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PathPatternConfigurer {
    private final List<String> includePatternList = new ArrayList<>();
    private final List<String> excludePatternList = new ArrayList<>();

    public void includePath(String path) {
        this.includePatternList.add(path);
    }

    public void includeAll() {
        this.includePatternList.add("/**");
    }

    public void excludePath(String path) {
        this.excludePatternList.add(path);
    }
}