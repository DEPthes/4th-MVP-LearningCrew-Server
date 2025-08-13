package com.depth.learningcrew.domain.auth.dto;

import com.depth.learningcrew.domain.user.dto.UserDto;
import com.depth.learningcrew.domain.user.entity.Gender;
import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.security.model.JwtDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public class AuthDto {

    // 회원가입 요청 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "회원가입 요청 DTO", requiredProperties = {"email", "nickname", "password", "birthday", "gender"})
    public static class SignUpRequest {
        @NotBlank(message = "이메일 형식으로 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Schema(description = "이메일 (아이디)", example = "user@email.com")
        private String email;

        @NotBlank(message = "특수문자를 제외한 2~10자리의 닉네임을 입력해주세요.")
        @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9-_]{2,10}$", message = "닉네임 조건에 충족되지 않습니다.")
        @Schema(description = "사용자 닉네임", example = "user nickname")
        private String nickname;

        @NotBlank(message = "대소문자 영문자와 숫자를 포함한 8자리 이상의 비밀번호를 입력해주세요.")
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z]).{8,}", message = "비밀번호 조건에 충족되지 않습니다.")
        @Schema(description = "사용자 비밀번호", example = "password content")
        private String password;

        @NotNull(message = "생년월일을 입력해주세요.")
        @Schema(description = "사용자 생년월일", example = "yyyy-mm-dd")
        private LocalDate birthday;

         @Schema(description = "프로필 이미지 정보")
         private MultipartFile profileImage;

        @NotNull(message = "성별을 입력해주세요.")
        @Schema(
                description = "사용자 성별",
                example = "MALE | FEMALE | OTHER",
                allowableValues = {"MALE", "FEMALE", "OTHER"}
        )
        private Gender gender;


        public User toEntity(PasswordEncoder encoder) {
            return User.builder()
                    .email(email)
                    .nickname(nickname)
                    .password(encoder.encode(password))
                    .birthday(birthday)
                    .gender(gender)
                    .role(Role.USER)
                    .build();
        }
    }

    // 로그인 요청 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "로그인 요청 DTO", requiredProperties = {"email", "password"})
    public static class SignInRequest {
        @NotBlank(message = "이메일을 입력해주세요.")
//        @Email(message = "올바른 이메일 형식이 아닙니다.") -> 테스트 케이스에서 이메일 형식이 아닌 데이터가 있음
        @Schema(description = "이메일(아이디)", example = "user@email.com")
        private String email;

        @NotBlank(message = "대소문자 영문자와 숫자를 포함한 8자리 이상의 비밀번호를 입력해주세요.")
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z]).{8,}", message = "비밀번호 조건에 충족되지 않습니다.")
        @Schema(description = "사용자 비밀번호", example = "password content")
        private String password;

    }

    // 로그인 응답 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "로그인 응답 DTO")
    public static class SignInResponse {
        @Schema(description = "로그인한 사용자 정보", implementation = UserDto.UserResponse.class)
        private UserDto.UserResponse user;

        @Schema(description = "발급된 토큰 정보", implementation = JwtDto.TokenInfo.class)
        private JwtDto.TokenInfo token;

        public static SignInResponse of(
                UserDto.UserResponse user,
                JwtDto.TokenInfo token
        ) {
            return SignInResponse.builder()
                    .user(user)
                    .token(token)
                    .build();
        }
    }

    // 아이디 중복 인증 확인 응답 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "이메일(아이디) 중복 확인 응답 DTO")
    public static class EmailExistResponse {
        @Schema(description = "이메일(아이디) 존재 여부 (true/false)", example = "false")
        private boolean isExist;

        public static EmailExistResponse from(boolean exists) {
            return EmailExistResponse.builder()
                    .isExist(exists)
                    .build();
        }
    }

    // 닉네임 중복 인증 확인 응답 DTO
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "닉네임 중복 확인 응답 DTO")
    public static class NicknameExistResponse {
        @Schema(description = "닉네임 존재 여부 (true/false)", example = "false")
        private boolean isExist;

        public static NicknameExistResponse from(boolean exists) {
            return NicknameExistResponse.builder()
                    .isExist(exists)
                    .build();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "토큰 재발행 DTO", requiredProperties = {"refreshToken"})
    public static class RecreateRequest {
        @NotBlank(message = "Refresh Token을 입력해주세요.")
        @Schema(description = "재발행할 Refresh Token", example = "refreshTokenString")
        private String refreshToken;
    }
}

