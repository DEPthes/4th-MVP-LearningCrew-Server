package com.depth.learningcrew.system.security.initializer;

import java.lang.reflect.Method;
import java.util.*;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.depth.learningcrew.system.security.annotation.NoJwtAuth;
import com.depth.learningcrew.system.security.model.ApiPathPattern;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtAuthPathInitializer implements ApplicationListener<ContextRefreshedEvent> {

  private final RequestMappingHandlerMapping handlerMapping;

  private final Set<ApiPathPattern> excludePaths = new HashSet<>();
  private final Set<ApiPathPattern> conflictPaths = new HashSet<>();
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  private volatile boolean initialized = false;

  public JwtAuthPathInitializer(
      @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
    this.handlerMapping = handlerMapping;
  }

  @Override
  public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
    if (!initialized) {
      synchronized (this) {
        if (!initialized) {
          scanAndCollectExcludePaths();
          scanAndCollectConflictingPaths();
          initialized = true;
        }
      }
    }
  }

  private void scanAndCollectConflictingPaths() {
    Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

    for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
      RequestMappingInfo mappingInfo = entry.getKey();

      Set<String> patterns = Objects.requireNonNull(mappingInfo.getPathPatternsCondition()).getPatternValues();
      if(patterns.isEmpty()) {
        continue;
      }

      Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();
      Optional<RequestMethod> firstMethod = methods.stream().findFirst();
      if (firstMethod.isEmpty()) {
        continue; // No methods defined for this mapping
      }

      patterns.forEach(pattern -> {
        ApiPathPattern apiPathPattern = ApiPathPattern.of(pattern, ApiPathPattern.METHODS.valueOf(firstMethod.get().name()));

        if (isAlreadyExcluded(apiPathPattern)) {
//          log.info("JWT 인증 제외 경로에 이미 존재하는 경로 발견: {} {}", firstMethod.get().name(), pattern);
          return; // 이미 제외된 경로는 무시
        }

        if (isConflictingPath(apiPathPattern)) {
          conflictPaths.add(apiPathPattern);
          log.warn("JWT 인증 제외 경로와 충돌하는 경로 발견: {} {}", firstMethod.get().name(), pattern);
        }
      });
    }

    if (!conflictPaths.isEmpty()) {
      log.warn("총 {}개의 충돌하는 경로가 발견되었습니다 인증 제외 목록에서 제외합니다: {}", conflictPaths.size(), conflictPaths);
    } else {
      log.info("충돌하는 경로가 없습니다.");
    }
  }

  private boolean isAlreadyExcluded(ApiPathPattern path) {
    for (ApiPathPattern existingPath : excludePaths) {
      if (existingPath.equals(path)) {
        return true;
      }
    }
    return false;
  }

  private boolean isConflictingPath(ApiPathPattern path) {
    for (ApiPathPattern existingPath : excludePaths) {
      if (pathMatcher.match(existingPath.getPattern(), path.getPattern())
          && existingPath.getMethod() == path.getMethod()) {
        return true;
      }
    }
    return false;
  }

  private void scanAndCollectExcludePaths() {
    Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

    for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
      RequestMappingInfo mappingInfo = entry.getKey();
      HandlerMethod handlerMethod = entry.getValue();

      if (shouldExcludeFromJwtAuth(handlerMethod)) {
        if (mappingInfo.getPathPatternsCondition() != null) { // Spring Boot 3.x
          Set<String> patterns = Objects.requireNonNull(mappingInfo.getPathPatternsCondition()).getPatternValues();
          if (!patterns.isEmpty()) {
            var method = mappingInfo.getMethodsCondition().getMethods().iterator().next();
            for (String pattern : patterns) {
              excludePaths.add(ApiPathPattern.of(pattern, ApiPathPattern.METHODS
                  .valueOf(method.name())));
            }
            log.info("JWT 인증 제외 경로 추가: {} {}, (메서드: {}.{})",
                method.name(),
                patterns,
                handlerMethod.getBeanType().getSimpleName(),
                handlerMethod.getMethod().getName());
          }
        }
      }
    }
    log.info("총 {}개의 경로가 JWT 인증에서 제외되었습니다: {}", excludePaths.size(), excludePaths);
  }

  private boolean shouldExcludeFromJwtAuth(HandlerMethod handlerMethod) {
    Method method = handlerMethod.getMethod();
    Class<?> beanType = handlerMethod.getBeanType();

    NoJwtAuth methodAnnotation = AnnotationUtils.findAnnotation(method, NoJwtAuth.class);
    if (methodAnnotation != null)
      return true;

    NoJwtAuth classAnnotation = AnnotationUtils.findAnnotation(beanType, NoJwtAuth.class);
    return classAnnotation != null;
  }

  public Set<ApiPathPattern> getExcludePaths() {
    return new HashSet<>(excludePaths);
  }

  public Set<ApiPathPattern> getConflictPaths() {
    return new HashSet<>(conflictPaths);
  }

  public void addExcludePath(String path, ApiPathPattern.METHODS method) {
    excludePaths.add(ApiPathPattern.of(path, method));
    log.info("런타임에 JWT 인증 제외 경로 추가: {}", path);
  }
}
