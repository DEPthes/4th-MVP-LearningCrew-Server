package com.depth.learningcrew.domain.auth.service;

import com.depth.learningcrew.domain.auth.dto.AuthDto;
import com.depth.learningcrew.domain.auth.repository.BlacklistTokenRepository;
import com.depth.learningcrew.domain.auth.repository.RedisRefreshTokenRepository;
import com.depth.learningcrew.domain.user.repository.UserRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.exception.JwtBlacklistedTokenException;
import com.depth.learningcrew.system.security.model.UserDetails;
import com.depth.learningcrew.system.security.utility.jwt.JwtTokenProvider;
import com.depth.learningcrew.system.security.utility.jwt.JwtTokenResolver;
import com.depth.learningcrew.system.security.utility.jwt.TokenType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisRefreshTokenRepository redisRefreshTokenRepository;
    private final JwtTokenResolver jwtTokenResolver;
    private final BlacklistTokenRepository blacklistTokenRepository;

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

        Object storedRefreshToken = redisRefreshTokenRepository.getById(id);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new RestException(ErrorCode.AUTH_TOKEN_INVALID);
        }

        // 기존 Refresh Token 제거 (RTR 정책)
        redisRefreshTokenRepository.deleteById(id);

        // Access Token -> Blacklist
        String accessToken = getAccessTokenFromRequest(httpRequest);
        long remainingExpiration = getRemainingExpiration(accessToken);
        blacklistTokenRepository.setByAtk(accessToken, remainingExpiration);

        if(!blacklistTokenRepository.existsByAtk(accessToken)) {
            throw new JwtBlacklistedTokenException("Access Token 이 블랙리스트에 등록되지 않았습니다.");
        }
// RefactoringPoint
//        var newAccessToken = jwtTokenProvider.createToken(userDetails, TokenType.ACCESS);
//        var newRefreshToken = jwtTokenProvider.createToken(userDetails, TokenType.REFRESH);
//
//        redisRefreshTokenRepository.setByIdAndRtk(id, newRefreshToken.getTokenString());
//
//        return AuthDto.TokenInfo.of(
//                newAccessToken.getTokenString(),
//                newRefreshToken.getTokenString(),
//                newAccessToken.getExpireAt(),
//                newRefreshToken.getExpireAt()
//        );

        return null; // RefactoringPoint
    }

    private String getAccessTokenFromRequest(HttpServletRequest request) {
        return jwtTokenResolver.parseTokenFromRequest(request)
                .orElseThrow(() -> new RestException(ErrorCode.AUTH_TOKEN_MISSING));
    }

    private long getRemainingExpiration(String accessToken){
        var parsed = jwtTokenResolver.resolveTokenFromString(accessToken);
        return Duration.between(
                LocalDateTime.now(),
                parsed.getExpireAt()
        ).getSeconds();
    }
}