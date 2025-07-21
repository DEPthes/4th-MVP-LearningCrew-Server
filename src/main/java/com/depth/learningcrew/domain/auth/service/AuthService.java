package com.depth.learningcrew.domain.auth.service;

import com.depth.learningcrew.domain.auth.dto.AuthDto;
import com.depth.learningcrew.domain.auth.token.dto.RefreshTokenDto;
import com.depth.learningcrew.domain.auth.token.entity.RefreshToken;
import com.depth.learningcrew.domain.auth.token.repository.RefreshTokenCacheRepository;
import com.depth.learningcrew.domain.auth.token.repository.RefreshTokenRepository;
import com.depth.learningcrew.domain.auth.token.validator.RefreshTokenValidator;
import com.depth.learningcrew.domain.user.dto.UserDto;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.domain.user.repository.UserRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.exception.JwtBlacklistedTokenException;
import com.depth.learningcrew.system.security.model.JwtDto;
import com.depth.learningcrew.system.security.model.UserDetails;
import com.depth.learningcrew.system.security.service.UserLoadService;
import com.depth.learningcrew.system.security.utility.jwt.JwtTokenProvider;
import com.depth.learningcrew.system.security.utility.jwt.JwtTokenResolver;
import com.depth.learningcrew.system.security.utility.jwt.TokenType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenResolver jwtTokenResolver;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenCacheRepository refreshTokenCacheRepository;
    private final RefreshTokenValidator refreshTokenValidator;
    private final UserLoadService userLoadService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public JwtDto.TokenPair recreateToken(AuthDto.RecreateRequest request){
        String refreshTokenUuid = request.getRefreshToken();
        String id = jwtTokenResolver.resolveTokenFromString(refreshTokenUuid).getSubject();

        refreshTokenValidator.validateOrThrow(id, refreshTokenUuid);
        refreshTokenRepository.deleteByUuid(refreshTokenUuid);
        refreshTokenCacheRepository.evictRefreshUuid(refreshTokenUuid);

        var userDetails = userLoadService.loadUserByKey(id)
                .orElseThrow(() -> new RestException(ErrorCode.AUTH_USER_NOT_FOUND));
        var tokenPair = jwtTokenProvider.createTokenPair(userDetails);

        String newRefreshUuid = tokenPair.getRefreshToken().getTokenString();
        RefreshToken newRefreshToken = RefreshTokenDto.toEntity(
                newRefreshUuid,
                userDetails.getKey(),
                tokenPair.getRefreshToken().getExpireAt()
        );

        refreshTokenRepository.save(newRefreshToken);
        refreshTokenCacheRepository.cacheRefreshUuid(newRefreshUuid, userDetails.getKey());

        return tokenPair;
    }

    @Transactional
    public UserDto.UserResponse signUp(AuthDto.SignUpRequest request) {
        boolean isExisting = userRepository.existsById(request.getId());
        if(isExisting)
            throw new RestException(ErrorCode.GLOBAL_ALREADY_EXIST);

        User toSave = request.toEntity(passwordEncoder);
        User saved = userRepository.save(toSave);

        return UserDto.UserResponse.from(saved);
    }

    @Transactional
    public AuthDto.SignInResponse signIn(AuthDto.SignInRequest request) {
        var found = userRepository.findById(request.getId())
                .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

        if(!passwordEncoder.matches(request.getPassword(), found.getPassword()))
            throw new RestException(ErrorCode.AUTH_PASSWORD_NOT_MATCH);

        var userDetails = UserDetails.from(found);

        var tokenPair = jwtTokenProvider.createTokenPair(userDetails);

        String refreshUuid = tokenPair.getRefreshToken().getTokenString();
        RefreshToken refreshToken = RefreshTokenDto.toEntity(
                refreshUuid,
                userDetails.getKey(),
                tokenPair.getRefreshToken().getExpireAt()
        );

        refreshTokenRepository.save(refreshToken);
        refreshTokenCacheRepository.cacheRefreshUuid(refreshUuid, userDetails.getKey());

        AuthDto.TokenInfo tokenInfo = AuthDto.TokenInfo.builder()
                .accessToken(tokenPair.getAccessToken().getTokenString())
                .refreshToken(tokenPair.getRefreshToken().getTokenString())
                .accessTokenExpiresAt(tokenPair.getAccessToken().getExpireAt())
                .refreshTokenExpiresAt(tokenPair.getRefreshToken().getExpireAt())
                .build();

        return AuthDto.SignInResponse.of(UserDto.UserResponse.from(found), tokenInfo);
    }
}