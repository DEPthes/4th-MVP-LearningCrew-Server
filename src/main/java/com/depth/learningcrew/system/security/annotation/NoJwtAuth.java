package com.depth.learningcrew.system.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JWT 인증을 제외할 API 메서드나 컨트롤러 클래스에 사용하는 어노테이션
 * 
 * 사용법:
 * 1. 메서드 레벨: 특정 메서드만 JWT 인증 제외
 * 2. 클래스 레벨: 해당 컨트롤러의 모든 메서드 JWT 인증 제외
 * 
 * @author Learning Crew
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface NoJwtAuth {

  /**
   * 설명 (선택사항)
   * 
   * @return 인증 제외 이유나 설명
   */
  String value() default "";
}