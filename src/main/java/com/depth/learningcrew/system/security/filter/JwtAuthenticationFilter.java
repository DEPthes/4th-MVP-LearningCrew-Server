package com.depth.learningcrew.system.security.filter;

import java.io.IOException;
import java.util.List;

import com.depth.learningcrew.system.security.initializer.JwtAuthPathInitializer;
import com.depth.learningcrew.system.security.model.ApiPathPattern;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.depth.learningcrew.domain.auth.token.validator.RefreshTokenValidator;
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

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final List<ApiPathPattern> ignorePatterns;
    private final List<ApiPathPattern> allowedPatterns;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private final JwtTokenResolver jwtTokenResolver;
    private final UserLoadService userLoadService;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final RefreshTokenValidator refreshTokenValidator;
    private final JwtAuthPathInitializer jwtAuthPathInitializer;

    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String servletPath = request.getServletPath();
        boolean requiresAuth = this.isMatchingURI(servletPath, request.getMethod());

        try {
            authenticateWithJwt(request, requiresAuth);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            if (requiresAuth) {
                handleAuthenticationError(request, response, e);
            } else {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
            }
        }
    }

    private void authenticateWithJwt(HttpServletRequest request, boolean requiresAuth) {
        var accessTokenOpt = jwtTokenResolver.parseTokenFromRequest(request);

        if (accessTokenOpt.isEmpty()) {
            if (requiresAuth) {
                throw new JwtTokenMissingException();
            }
            return;
        }

        String accessToken = accessTokenOpt.get();

        if (!jwtTokenResolver.validateToken(accessToken)) {
            if (requiresAuth) {
                throw new JwtBlacklistedTokenException();
            }
            return;
        }

        var parsedTokenData = jwtTokenResolver.resolveTokenFromString(accessToken);
        var userDetails = userLoadService.loadUserByKey(parsedTokenData.getSubject());

        if (userDetails.isEmpty()) {
            if (requiresAuth) {
                throw new JwtInvalidTokenException();
            }
            return;
        }

        // Refresh UUID 검증
        refreshTokenValidator.validateOrThrow(userDetails.get().getKey(), parsedTokenData.getRefreshUuid());

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                userDetails.get(),
                                null,
                                userDetails.get().getAuthorities()));
    }

    private void handleAuthenticationError(HttpServletRequest request, HttpServletResponse response, Exception e) {
        if (e instanceof JwtAuthenticationException) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        } else {
            handlerExceptionResolver.resolveException(
                    request, response, null,
                    new JwtAuthenticationException("Authentication failed", 401, e));
        }
    }

    private boolean isMatchingURI(String servletPath, String method) {
        ApiPathPattern.METHODS apiMethod = ApiPathPattern.METHODS.parse(method);

        if (apiMethod == null) {
            return false;
        }

        boolean isAllowed = allowedPatterns.stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern.getPattern(), servletPath) && pattern.getMethod() == apiMethod);

        boolean isIgnored = ignorePatterns.stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern.getPattern(), servletPath) && pattern.getMethod() == apiMethod);

        // 어노테이션을 통해 제외된 경로 확인
        boolean isExcluded = jwtAuthPathInitializer.getExcludePaths().stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern.getPattern(), servletPath) && pattern.getMethod() == apiMethod);

        return isAllowed && !isIgnored && !isExcluded;
    }
}