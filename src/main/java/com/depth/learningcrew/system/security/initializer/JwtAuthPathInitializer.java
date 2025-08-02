package com.depth.learningcrew.system.security.initializer;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.depth.learningcrew.system.security.annotation.NoJwtAuth;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtAuthPathInitializer implements ApplicationListener<ContextRefreshedEvent> {

  private final RequestMappingHandlerMapping handlerMapping;

  private final Set<String> excludePaths = new HashSet<>();
  private volatile boolean initialized = false;

  public JwtAuthPathInitializer(
          @Qualifier("requestMappingHandlerMapping")
          RequestMappingHandlerMapping handlerMapping
  ) {
    this.handlerMapping = handlerMapping;
  }

  @Override
  public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
    if (!initialized) {
      synchronized (this) {
        if (!initialized) {
          scanAndCollectExcludePaths();
          initialized = true;
        }
      }
    }
  }

  private void scanAndCollectExcludePaths() {
    Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

    for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
      RequestMappingInfo mappingInfo = entry.getKey();
      HandlerMethod handlerMethod = entry.getValue();

      if (shouldExcludeFromJwtAuth(handlerMethod)) {
        if (mappingInfo.getPathPatternsCondition() != null) { // Spring Boot 3.x
          Set<String> patterns = mappingInfo.getPathPatternsCondition().getPatternValues();
          if (patterns != null && !patterns.isEmpty()) {
            excludePaths.addAll(patterns);
            log.info("JWT 인증 제외 경로 추가: {} (메서드: {}.{})",
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
    if (methodAnnotation != null) return true;

    NoJwtAuth classAnnotation = AnnotationUtils.findAnnotation(beanType, NoJwtAuth.class);
    return classAnnotation != null;
  }

  public Set<String> getExcludePaths() {
    return new HashSet<>(excludePaths);
  }

  public void addExcludePath(String path) {
    excludePaths.add(path);
    log.info("런타임에 JWT 인증 제외 경로 추가: {}", path);
  }
}
