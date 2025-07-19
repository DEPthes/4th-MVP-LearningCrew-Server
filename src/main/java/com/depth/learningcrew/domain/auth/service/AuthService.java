package com.depth.learningcrew.domain.auth.service;

import com.depth.learningcrew.domain.auth.dto.AuthDto;
import com.depth.learningcrew.domain.auth.repository.RefreshTokenRepository;
import com.depth.learningcrew.domain.user.repository.UserRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.exception.JwtBlacklistedTokenException;
import com.depth.learningcrew.system.security.model.UserDetails;
import com.depth.learningcrew.system.security.utility.jwt.JwtTokenProvider;
import com.depth.learningcrew.system.security.utility.jwt.JwtTokenResolver;
import com.depth.learningcrew.system.security.utility.jwt.TokenType;
import com.depth.learningcrew.system.security.utility.redis.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisUtil redisUtil;
    private final JwtTokenResolver jwtTokenResolver;

    @Transactional
    public AuthDto.TokenInfo recreateToken(
            AuthDto.RecreateRequest recreateRequest,
            HttpServletRequest httpRequest
    ){
        String id = recreateRequest.getId();
        String refreshToken = recreateRequest.getRefreshToken();

        var userDetails = userRepository.findById(id)
                .map(UserDetails::from)
                .orElseThrow(() -> new RestException(ErrorCode.AUTH_USER_NOT_FOUND));

        Object storedRefreshToken = refreshTokenRepository.getById(id);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new RestException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        // 기존 Refresh Token 제거 (RTR 정책)
        refreshTokenRepository.deleteById(id);

        // Access Token -> Blacklist
        String accessToken = jwtTokenResolver.parseTokenFromRequest(httpRequest)
                .orElseThrow(() -> new RestException(ErrorCode.AUTH_TOKEN_MISSING));
        redisUtil.setBlackList(accessToken, TokenType.ACCESS); //

        if(!redisUtil.hasKeyBlackList(accessToken)) {
            throw new JwtBlacklistedTokenException("Access Token 이 블랙리스트에 등록되지 않았습니다.");
        }

        var newAccessToken = jwtTokenProvider.createToken(userDetails, TokenType.ACCESS);
        var newRefreshToken = jwtTokenProvider.createToken(userDetails, TokenType.REFRESH);

        refreshTokenRepository.setByIdAndRtk(id, newRefreshToken);

        return AuthDto.TokenInfo.of(
                newAccessToken.getTokenString(),
                newRefreshToken.getTokenString(),
                newAccessToken.getExpireAt(),
                newRefreshToken.getExpireAt()
        );
    }
}