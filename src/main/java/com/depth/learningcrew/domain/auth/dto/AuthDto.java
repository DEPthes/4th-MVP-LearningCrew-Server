package com.depth.learningcrew.domain.auth.dto;

import com.depth.learningcrew.domain.user.dto.UserDto;
import com.depth.learningcrew.domain.user.entity.Gender;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.security.model.JwtDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AuthDto {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "토큰 재발행 DTO")
    public static class RecreateRequest {
        @NotBlank(message = "Refresh Token을 입력해주세요.")
        @Schema(description = "재발행할 Refresh Token", example = "refreshTokenString")
        private String refreshToken;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "토큰 정보 DTO")
    public static class TokenInfo {
        @Schema(description = "발급된 Access Token", example = "ehJk2Y3Rlc3Q1qW.NjZXNzVG9rZW4=")
        private String accessToken;
        @Schema(description = "발급된 Refresh Token", example = "ehJk2Y3Rlc3Q1qW.Aw5pdGlhbE9iamVjdA==")
        private String refreshToken;
        @Schema(description = "Access Token 만료 시간", example = "2023-10-01T12:00:00")
        private LocalDateTime accessTokenExpiresAt;
        @Schema(description = "Refresh Token 만료 시간", example = "2023-12-01T12:00:00")
        private LocalDateTime refreshTokenExpiresAt;

        public static TokenInfo of(
                String accessToken,
                String refreshToken,
                LocalDateTime accessTokenExpiresAt,
                LocalDateTime refreshTokenExpiresAt
        ) {
            return TokenInfo.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .accessTokenExpiresAt(accessTokenExpiresAt)
                    .refreshTokenExpiresAt(refreshTokenExpiresAt)
                    .build();
        }
    }

    // 회원가입 요청 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "회원가입 요청 DTO")
    public static class SignUpRequest {
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Schema(description = "사용자 아이디(이메일 형식)", example = "learnit@mju.ac.kr")
        private String id;

        @NotBlank(message = "특수문자를 제외한 2~10자리의 닉네임을 입력해주세요.")
        @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9-_]{2,10}$", message = "닉네임 조건에 충족되지 않습니다.")
        @Schema(description = "사용자 닉네임", example = "LearnIt팀1")
        private String nickname;

        @NotBlank(message = "대소문자 영문자와 숫자를 포함한 8자리 이상의 비밀번호를 입력해주세요.")
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z]).{8,}", message = "비밀번호 조건에 충족되지 않습니다.")
        @Schema(description = "사용자 비밀번호", example = "newpassword123 || NewPassword123")
        private String password;

        @NotNull(message = "생년월일을 입력해주세요.")
        @Schema(description = "사용자 생년월일", example = "2000-01-01")
        private LocalDate birthday;

        @NotNull(message = "성별을 입력해주세요.")
        @Schema(
                description = "사용자 성별",
                example = "MALE",
                allowableValues = {"MALE", "FEMALE", "OTHER"}
        )
        private Gender gender;


        public User toEntity(PasswordEncoder encoder) {
            return User.builder()
                    .id(id)
                    .nickname(nickname)
                    .password(encoder.encode(password))
                    .birthday(birthday)
                    .gender(gender)
                    .build();
        }
    }

    // 로그인 요청 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "로그인 요청 DTO")
    public static class SignInRequest {
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Schema(description = "사용자 아이디(이메일 형식)", example = "learnit@mju.ac.kr")
        private String id;

        @NotBlank(message = "대소문자 영문자와 숫자를 포함한 8자리 이상의 비밀번호를 입력해주세요.")
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z]).{8,}", message = "비밀번호 조건에 충족되지 않습니다.")
        @Schema(description = "사용자 비밀번호", example = "newpassword123 || NewPassword123")
        private String password;

    }

    // 로그인 응답 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "로그인 응답 DTO")
    public static class SignInResponse {
        @Schema(description = "발급된 토큰 정보", implementation = TokenInfo.class)
        private AuthDto.TokenInfo token;

        @Schema(description = "로그인한 사용자 정보", implementation = UserDto.UserResponse.class)
        private UserDto.UserResponse user;

        public static SignInResponse of(
                UserDto.UserResponse user,
                AuthDto.TokenInfo token
        ) {
            return SignInResponse.builder()
                    .user(user)
                    .token(token)
                    .build();
        }
    }

    // 아이디 중복 인증 확인 요청 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "아이디 중복 확인 요청 DTO")
    public static class IdExistRequest {
        @NotBlank(message = "확인할 아이디(이메일 주소)를 입력해주세요.")
        @Schema(description = "확인할 아이디(이메일 주소)", example = "learnit@mju.ac.kr")
        private String id;
    }

    // 아이디 중복 인증 확인 응답 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "아이디 중복 확인 응답 DTO")
    public static class IdExistResponse {
        @Schema(description = "아이디 존재 여부 (true/false)", example = "false")
        private boolean isExist;

        public static IdExistResponse from(boolean exists) {
            return IdExistResponse.builder()
                    .isExist(exists)
                    .build();
        }
    }
}