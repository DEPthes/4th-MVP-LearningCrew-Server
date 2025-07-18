package com.depth.learningcrew.domain.user.entity;

import com.depth.learningcrew.common.auditor.TimeStampedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "USER")
public class User extends TimeStampedEntity {
    @Id @Setter(AccessLevel.NONE)
    @Column(name= "id", length = 50)
    private String email;

    @Column(nullable=false, length = 50)
    private String password;

    @Column(nullable=false, length = 30)
    private String nickname;

    @Column(nullable=false)
    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length = 20)
    private Gender gender;
}
