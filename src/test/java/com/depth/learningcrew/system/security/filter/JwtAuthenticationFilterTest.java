package com.depth.learningcrew.system.security.filter;

import com.depth.learningcrew.domain.auth.dto.AuthDto;
import com.depth.learningcrew.domain.auth.service.AuthService;
import com.depth.learningcrew.domain.auth.service.RefreshTokenService;
import com.depth.learningcrew.domain.user.entity.Gender;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.domain.user.repository.UserRepository;
import com.depth.learningcrew.system.exception.handler.GlobalExceptionHandler;
import com.depth.learningcrew.system.security.exception.JwtAuthenticationException;
import com.depth.learningcrew.system.security.exception.JwtInvalidTokenException;
import com.depth.learningcrew.system.security.model.UserDetails;
import com.depth.learningcrew.system.security.service.UserLoadService;
import com.depth.learningcrew.system.security.utility.jwt.JwtTokenProvider;
import com.depth.learningcrew.system.security.utility.jwt.JwtTokenResolver;
import com.depth.learningcrew.system.security.utility.jwt.TokenType;
import com.depth.learningcrew.system.security.utility.redis.RedisUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JwtAuthenticationFilterTest {

    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired RefreshTokenService refreshTokenService;
    @Autowired RedisUtil redisUtil;
    @Autowired UserLoadService userLoadService;
    @Autowired JwtTokenResolver jwtTokenResolver;
    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;

    JwtAuthenticationFilter jwtAuthenticationFilter;
    HandlerExceptionResolver exceptionResolver;

    String userId = "testUser";
    String accessToken, refreshToken;

    @BeforeEach
    void setUp() {
        exceptionResolver = mock(HandlerExceptionResolver.class);

        jwtAuthenticationFilter = new JwtAuthenticationFilter(
                List.of(), // ignorePatterns
                List.of("/secured/**"), // allowedPatterns
                jwtTokenResolver,
                userLoadService,
                exceptionResolver
        );

        User user = User.builder()
                .id(userId)
                .password("password")
                .nickname("tester")
                .birthday(LocalDate.of(2000, 1, 1))
                .gender(Gender.MALE)
                .build();

        userRepository.save(user);

        UserDetails userDetails = UserDetails.from(user);

        accessToken = jwtTokenProvider.createToken(userDetails, TokenType.ACCESS).getTokenString();
        refreshToken = jwtTokenProvider.createToken(userDetails, TokenType.REFRESH).getTokenString();

        refreshTokenService.storeRefreshToken(userId, refreshToken);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @Order(1)
    @DisplayName("1. Access Token과 Refresh Token이 잘 반환되는가?")
    void testTokenCreation() {
        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();

        String storedRefresh = (String) refreshTokenService.getRefreshToken(userId);
        assertThat(storedRefresh).isEqualTo(refreshToken);
    }

    @Test
    @Order(2)
    @DisplayName("2. Refresh Token으로 Access/Refresh Token 재발행 후 검증")
    void testTokenRecreationProperly() {
        refreshTokenService.deleteRefreshToken(userId);
        assertThat(refreshTokenService.getRefreshToken(userId)).isNull();

        UserDetails userDetails = userRepository.findById(userId)
                .map(UserDetails::from)
                .orElseThrow();

        var newAccessToken = jwtTokenProvider.createToken(userDetails, TokenType.ACCESS);
        var newRefreshToken = jwtTokenProvider.createToken(userDetails, TokenType.REFRESH);

        refreshTokenService.storeRefreshToken(userId, newRefreshToken.getTokenString());

        String storedRefresh = (String) refreshTokenService.getRefreshToken(userId);
        assertThat(storedRefresh).isEqualTo(newRefreshToken.getTokenString());

        boolean isAccessTokenValid = jwtTokenResolver.validateToken(newAccessToken.getTokenString());
        assertThat(isAccessTokenValid).isTrue();
    }

    @Test
    @Order(3)
    @DisplayName("3. recreateToken - Refresh 삭제 + Access Blacklist 등록 + 재발행 검증")
    void testRecreateTokenDeletesRefreshAndBlacklistsAccess() {
        // given
        AuthDto.RecreateRequest recreateRequest = AuthDto.RecreateRequest.builder()
                .id(userId)
                .refreshToken(refreshToken)
                .build();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + accessToken);

        // when
        AuthDto.TokenInfo tokenInfo = authService.recreateToken(recreateRequest, request);

        // then
        // 1. 이전 Refresh Token 삭제 여부
        assertThat(refreshTokenService.getRefreshToken(userId)).isEqualTo(tokenInfo.getRefreshToken());

        // 2. Access Token Blacklist 등록 확인
        assertThat(redisUtil.hasKeyBlackList(accessToken)).isTrue();

        // 3. 새 Access/Refresh Token 이 정상 생성되었는지
        assertThat(tokenInfo.getAccessToken()).isNotBlank();
        assertThat(tokenInfo.getRefreshToken()).isNotBlank();
        assertThat(tokenInfo.getAccessTokenExpiresAt()).isNotNull();
        assertThat(tokenInfo.getRefreshTokenExpiresAt()).isNotNull();
    }

    @Test
    @Order(4)
    @DisplayName("4. Access Token Blacklist TTL은 30분")
    void testBlacklistTTL() {
        redisUtil.setBlackList(accessToken, 30);

        Long ttl = redisUtil.getExpire(accessToken, TimeUnit.MINUTES);
        assertThat(ttl).isBetween(29L, 30L);
    }

    @Test
    @Order(5)
    @DisplayName("5. JwtAuthenticationFilter - 무효한 토큰 요청시 차단")
    void testInvalidTokenBlocked() throws ServletException, IOException {
        // 별도 mock 준비
        JwtTokenResolver mockJwtTokenResolver = mock(JwtTokenResolver.class);

        // 필터 재생성
        jwtAuthenticationFilter = new JwtAuthenticationFilter(
                List.of(),
                List.of("/secured/**"),
                mockJwtTokenResolver,
                userLoadService,
                exceptionResolver
        );

        String invalidToken = "invalid.jwt.token";

        when(mockJwtTokenResolver.parseTokenFromRequest(any())).thenReturn(Optional.of(invalidToken));
        when(mockJwtTokenResolver.validateToken(invalidToken)).thenThrow(new JwtInvalidTokenException());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/secured/test");
        request.addHeader("Authorization", "Bearer " + invalidToken);

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(exceptionResolver, times(1)).resolveException(
                eq(request), eq(response),
                isNull(), any(JwtAuthenticationException.class)
        );
        verifyNoInteractions(filterChain);
    }

    @Test
    @Order(6)
    @DisplayName("6. JwtAuthenticationFilter - Blacklist 토큰 요청시 SecurityContext 비어있음")
    void testBlacklistedTokenBlocked() throws ServletException, IOException {
        JwtTokenResolver mockJwtTokenResolver = mock(JwtTokenResolver.class);

        jwtAuthenticationFilter = new JwtAuthenticationFilter(
                List.of(),
                List.of("/secured/**"),
                mockJwtTokenResolver,
                userLoadService,
                exceptionResolver
        );

        redisUtil.setBlackList(accessToken, 30);

        when(mockJwtTokenResolver.parseTokenFromRequest(any())).thenReturn(Optional.of(accessToken));
        when(mockJwtTokenResolver.validateToken(accessToken)).thenCallRealMethod();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/secured/test");
        request.addHeader("Authorization", "Bearer " + accessToken);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}