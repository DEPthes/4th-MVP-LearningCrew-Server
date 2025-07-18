package com.depth.learningcrew.system.security.filter;

import com.depth.learningcrew.system.security.exception.JwtAuthenticationException;
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

                // var 쓰는 기준 : 타입이 뻔히 보이는 경우, 길어서 가독성 해치는 경우
                var parsedTokenData = jwtTokenResolver.resolveTokenFromString(token);
                var userDetails = userLoadService.loadUserByKey(parsedTokenData.getSubject());

                if(userDetails.isEmpty()) {
                    throw new JwtInvalidTokenException();
                }

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