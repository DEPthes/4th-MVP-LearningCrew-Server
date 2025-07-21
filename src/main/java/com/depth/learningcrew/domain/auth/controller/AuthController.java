package com.depth.learningcrew.domain.auth.controller;

import com.depth.learningcrew.domain.auth.dto.AuthDto;
import com.depth.learningcrew.domain.auth.service.AuthService;
import com.depth.learningcrew.domain.user.dto.UserDto;
import com.depth.learningcrew.system.security.model.JwtDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증/인가 API")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "회원가입 성공")
    public UserDto.UserResponse signUp(@RequestBody @Valid AuthDto.SignUpRequest request) {
        return authService.signUp(request);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    public AuthDto.SignInResponse signIn(@RequestBody @Valid AuthDto.SignInRequest request) {
        return authService.signIn(request);
    }

    @GetMapping("/id-exist")
    @Operation(summary = "아이디 중복 확인", description = "입력된 아이디의 사용 가능 여부를 확인합니다.")
    @ApiResponse(responseCode = "200", description = "아이디 확인 성공")
    public AuthDto.IdExistResponse checkId(@RequestBody @Valid AuthDto.IdExistRequest request) {
        return authService.checkIdExist(request);
    }

    @PostMapping("/token/refresh")
    @Operation(summary = "토큰 재발행", description = "새로운 Access/Refresh Token을 재발급받습니다.")
    @ApiResponse(responseCode = "200", description = "토큰 재발행 성공")
    public JwtDto.TokenInfo refreshToken(@RequestBody @Valid AuthDto.RecreateRequest request) {
        return authService.recreateToken(request);
    }
}
