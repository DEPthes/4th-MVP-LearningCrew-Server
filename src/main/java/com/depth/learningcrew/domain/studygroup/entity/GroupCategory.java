package com.depth.learningcrew.domain.studygroup.entity;

import java.util.ArrayList;
import java.util.List;

import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Integer id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "categories")
    @Builder.Default
    private List<StudyGroup> studyGroups = new ArrayList<>();

    public void canUpdateBy(UserDetails user) {
        if(user.getUser().getRole().equals(Role.ADMIN)) {
            return;
        }

        throw new RestException(ErrorCode.AUTH_FORBIDDEN);
    }
}
