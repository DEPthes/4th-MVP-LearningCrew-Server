package com.depth.learningcrew.domain.user.entity;

import com.depth.learningcrew.common.auditor.TimeStampedEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "UserAccount")
public class User extends TimeStampedEntity {
    @Id @Setter(AccessLevel.NONE)
    private String email;

    private String password;

    private String name;
}
