package com.depth.learningcrew.system.security.filter;

import com.depth.learningcrew.system.security.exception.JwtAuthenticationException;
import com.depth.learningcrew.system.security.exception.JwtBlacklistedTokenException;
import com.depth.learningcrew.system.security.exception.JwtInvalidTokenException;
import com.depth.learningcrew.system.security.exception.JwtTokenMissingException;
import com.depth.learningcrew.system.security.service.UserLoadService;
import com.depth.learningcrew.system.security.utility.jwt.JwtTokenResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final List<String> ignorePatterns;
    private final List<String> allowedPatterns;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private final JwtTokenResolver jwtTokenResolver;
    private final UserLoadService userLoadService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String servletPath = request.getServletPath();

        if (this.isMatchingURI(servletPath)) {
            try {
                String token = jwtTokenResolver.parseTokenFromRequest(request)
                        .orElseThrow(JwtTokenMissingException::new);

                if (!jwtTokenResolver.validateToken(token)) {throw new JwtBlacklistedTokenException();}

                var parsedTokenData = jwtTokenResolver.resolveTokenFromString(token);
                var userDetails = userLoadService.loadUserByKey(parsedTokenData.getSubject());

                if(userDetails.isEmpty()) {throw new JwtInvalidTokenException();}

                /* 추후 리팩토링 고려 지점 -> JwtTokenProvider에서 getAuthentication을 하는게 가독성 측면에선 깔끔
                *  Resolver와 Provider의 역할을 분리하는게 맞는지 고민 필요
                *  SRP에 의하면 분리하는게 맞긴한데, 가독성까지 챙겨가자니 DI가 너무 가중됨
                *  만약 Provider에서 Validation과 getAuthentication을 모두 처리한다면 8줄을 날릴 수 있음
                *  대신 Provider가 너무 많은 책임을 가지게 됨
                */
                SecurityContextHolder.getContext()
                        .setAuthentication(
                                new UsernamePasswordAuthenticationToken(
                                        userDetails.get(),
                                        null,
                                        userDetails.get().getAuthorities()
                                )
                        );

                filterChain.doFilter(request, response);
            }catch (Exception e) {
                if (e instanceof JwtAuthenticationException) {
                    handlerExceptionResolver.resolveException(request, response, null, e);
                } else {
                    handlerExceptionResolver.resolveException(
                            request, response, null,
                            new JwtAuthenticationException("Authentication failed", 401, e));
                }
            }
        }else{
            filterChain.doFilter(request, response);
        }
    }

    private boolean isMatchingURI(String servletPath) {
        boolean isAllowed = allowedPatterns.stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, servletPath));

        boolean isIgnored = ignorePatterns.stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, servletPath));

        return isAllowed && !isIgnored;
    }
}