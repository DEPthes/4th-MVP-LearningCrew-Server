package com.depth.learningcrew.system.security.configurer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Getter;

@Getter
public class PathPatternConfigurer {
    private final List<String> includePatternList = new ArrayList<>();
    private final List<String> excludePatternList = new ArrayList<>();

    /**
     * 포함할 경로 패턴 추가
     * 
     * @param path 포함할 경로
     */
    public void includePath(String path) {
        this.includePatternList.add(path);
    }

    /**
     * 모든 경로 포함
     */
    public void includeAll() {
        this.includePatternList.add("/**");
    }

    /**
     * 제외할 경로 패턴 추가
     * 
     * @param path 제외할 경로
     */
    public void excludePath(String path) {
        this.excludePatternList.add(path);
    }

    /**
     * 여러 경로를 한번에 제외 패턴에 추가
     * 
     * @param paths 제외할 경로들
     */
    public void excludePaths(Collection<String> paths) {
        this.excludePatternList.addAll(paths);
    }

    /**
     * 여러 경로를 한번에 포함 패턴에 추가
     * 
     * @param paths 포함할 경로들
     */
    public void includePaths(Collection<String> paths) {
        this.includePatternList.addAll(paths);
    }

    /**
     * 현재 설정된 제외 패턴 개수 반환
     * 
     * @return 제외 패턴 개수
     */
    public int getExcludePatternCount() {
        return excludePatternList.size();
    }

    /**
     * 현재 설정된 포함 패턴 개수 반환
     * 
     * @return 포함 패턴 개수
     */
    public int getIncludePatternCount() {
        return includePatternList.size();
    }
}