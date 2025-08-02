package com.depth.learningcrew.system.configuration.security;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.depth.learningcrew.system.security.configurer.JwtAutoConfigurerFactory;
import com.depth.learningcrew.system.security.initializer.JwtAuthPathInitializer;
import com.depth.learningcrew.system.security.service.UserLoadServiceImpl;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAutoConfigurerFactory jwtAutoConfigurerFactory;
    private final JwtAuthPathInitializer jwtAuthPathInitializer;

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity,
            UserLoadServiceImpl userLoadServiceImpl) throws Exception {
        jwtAutoConfigurerFactory.create(userLoadServiceImpl)
                .pathConfigure((it) -> {
                    // 기본 포함 경로 설정
                    it.includePath("/api/**");

                    // @NoJwtAuth 어노테이션으로 자동 수집된 경로들 추가
                    it.excludePaths(jwtAuthPathInitializer.getExcludePaths());
                })
                .configure(httpSecurity);

        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigSrc() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}