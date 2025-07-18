package com.depth.learningcrew.system.security.configurer;

import com.depth.learningcrew.system.security.filter.JwtAuthenticationFilter;
import com.depth.learningcrew.system.security.service.UserLoadService;
import com.depth.learningcrew.system.security.utility.JwtTokenResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@RequiredArgsConstructor
@Slf4j
public class JwtAutoConfigurer {
    private final JwtTokenResolver jwtTokenResolver;
    private final UserLoadService userLoadService;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final PathPatternConfigurer pathPatternConfigurer = new PathPatternConfigurer();

    public JwtAutoConfigurer pathConfigure(Customizer<PathPatternConfigurer> customizer) {
        customizer.customize(this.pathPatternConfigurer);
        return this;
    }

    public void configure(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(
                pathPatternConfigurer.getExcludePatternList(),
                pathPatternConfigurer.getIncludePatternList(),
                jwtTokenResolver,
                userLoadService,
                handlerExceptionResolver
        );

        http
                .headers(it -> it.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(it -> it.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("Jwt 인증 자동 설정이 완료되었습니다.");
    }
}