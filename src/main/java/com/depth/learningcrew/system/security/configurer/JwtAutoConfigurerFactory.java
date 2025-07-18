package com.depth.learningcrew.system.security.configurer;

import com.depth.learningcrew.system.security.service.UserLoadService;
import com.depth.learningcrew.system.security.utility.JwtTokenResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerExceptionResolver;

@RequiredArgsConstructor
public class JwtAutoConfigurerFactory {
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtTokenResolver jwtTokenResolver;

    public JwtAutoConfigurer create(UserLoadService userLoadService) {
        return new JwtAutoConfigurer(jwtTokenResolver, userLoadService, handlerExceptionResolver);
    }
}