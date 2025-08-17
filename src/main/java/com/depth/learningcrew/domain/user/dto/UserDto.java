package com.depth.learningcrew.domain.user.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import com.depth.learningcrew.domain.file.dto.FileDto;
import com.depth.learningcrew.domain.user.entity.Gender;
import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.domain.user.entity.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserDto {
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class UserResponse {
        @Schema(description = "사용자 ID", example = "1")
        private Long id;
        @Schema(description = "이메일(아이디)", example = "user@email.com")
        private String email;
        @Schema(description = "사용자 닉네임", example = "user nickname")
        private String nickname;
        @Schema(description = "사용자 역할", example = "USER")
        private Role role;
        @Schema(description = "사용자 성별", example = "MALE | FEMAIL | OTHER")
        private Gender gender;
        @Schema(description = "사용자 프로필 이미지")
        private FileDto.FileResponse profileImage;
        @Schema(description = "사용자 생일", example = "YYYY-MM-DD")
        private LocalDate birthday;
        @Schema(description = "계정 생성 시간", example = "ISO Datetime")
        private LocalDateTime createdAt;
        @Schema(description = "마지막 정보 수정 시간", example = "ISO Datetime")
        private LocalDateTime lastModifiedAt;

        public static UserResponse from(User user) {
            return UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .role(user.getRole())
                    .gender(user.getGender())
                    .profileImage(FileDto.FileResponse.from(user.getProfileImage()))
                    .birthday(user.getBirthday())
                    .createdAt(user.getCreatedAt())
                    .lastModifiedAt(user.getLastModifiedAt())
                    .build();
        }
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class UserUpdateRequest {
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Schema(description = "이메일(아이디)", example = "user@email.com")
        private String email;

        @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9-_]{2,10}$", message = "닉네임 조건에 충족되지 않습니다.")
        @Schema(description = "사용자 닉네임", example = "user nickname")
        private String nickname;

        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z]).{8,}", message = "비밀번호 조건에 충족되지 않습니다.")
        @Schema(description = "사용자 비밀번호", example = "password content")
        private String password;

        @Schema(description = "사용자 프로필 이미지", example = "MultipartFile", type = "file")
        private MultipartFile profileImage;

        public void applyTo(User user, PasswordEncoder encoder) {
            if (email != null && !email.equals(user.getEmail())) {
                user.setEmail(email);
            }
            if (nickname != null && !nickname.equals(user.getNickname())) {
                user.setNickname(nickname);
            }
            if (password != null) {
                user.setPassword(encoder.encode(password));
            }
        }
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class UserUpdateResponse {
        @Schema(description = "이메일(아이디)", example = "user@example.com")
        private String email;
        @Schema(description = "사용자 닉네임", example = "user nickname")
        private String nickname;
        @Schema(description = "사용자 역할", example = "USER | ADMIN")
        private Role role;
        @Schema(description = "계정 생성 시간", example = "ISO Datetime")
        private LocalDateTime createdAt;
        @Schema(description = "마지막 정보 수정 시간", example = "ISO Datetime")
        private LocalDateTime lastModifiedAt;

        public static UserUpdateResponse from(User user) {
            return UserUpdateResponse.builder()
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .role(user.getRole())
                    .createdAt(user.getCreatedAt())
                    .lastModifiedAt(user.getLastModifiedAt())
                    .build();
        }
    }
}
