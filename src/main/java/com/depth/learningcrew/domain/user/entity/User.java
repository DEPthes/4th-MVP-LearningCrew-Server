package com.depth.learningcrew.domain.user.entity;

import com.depth.learningcrew.common.auditor.TimeStampedEntity;
import com.depth.learningcrew.domain.user.dto.UserDto;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(
    name = "USER_ACCOUNT",
    uniqueConstraints = {
        @UniqueConstraint(name = "USER_NICKNAME", columnNames = "nickname"),
        @UniqueConstraint(name = "USER_EMAIL", columnNames = "email")
    }
)
public class User extends TimeStampedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable=false, length = 50)
    private String email;

    @Column(nullable=false, length = 60)
    private String password;

    // 대소문자 구분해서 저장하도록 COLLATE 설정 (단, 이는 MySQL, MariaDB에서 가능)(ex. Hooby <-> hooby)
    @Column(nullable=false, length = 30, columnDefinition = "VARCHAR(30) COLLATE utf8mb4_bin")
    private String nickname;

    @Column(nullable=false)
    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length = 20)
    private Role role = Role.USER;

    // TODO: AttachedFile Entity 구현 이후 연관관계 매핑 적용

    public void update(UserDto.UserUpdateRequest request, PasswordEncoder encoder) {
        if (!this.email.equals(request.getEmail())) {this.email = request.getEmail();}

        if (!this.nickname.equals(request.getNickname())) {this.nickname = request.getNickname();}

        this.password = encoder.encode(request.getPassword());
    }
}