package com.depth.learningcrew.domain.user.entity;

import com.depth.learningcrew.common.auditor.TimeStampedEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(
    name = "USER_ACCOUNT",
    uniqueConstraints = {
        @UniqueConstraint(name = "USER_NICKNAME", columnNames = "nickname")
    }
)
public class User extends TimeStampedEntity {
    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable=false, length = 60)
    private String password;

    @Column(nullable=false, length = 30)
    private String nickname;

    @Column(nullable=false)
    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length = 20)
    private Role role = Role.USER;
}
