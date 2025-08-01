package com.depth.learningcrew.domain.studygroup.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.depth.learningcrew.common.auditor.TimeStampedEntity;
import com.depth.learningcrew.domain.file.entity.StudyGroupImage;
import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class StudyGroup extends TimeStampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Integer id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, length = 255)
    private String summary;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer maxMembers;

    @Column(nullable = false)
    private Integer memberCount;

    @Column(nullable = false)
    private Integer currentStep;

    @ManyToMany
    @JoinTable(name = "CATEGORY_GROUP_MAPPING", joinColumns = @JoinColumn(name = "study_group_id"), inverseJoinColumns = @JoinColumn(name = "group_category_id"))
    @Builder.Default
    private List<GroupCategory> categories = new ArrayList<>();

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "owner_id")
    private User owner;

    @OneToOne(mappedBy = "studyGroup")
    private StudyGroupImage studyGroupImage;

    @OneToMany(mappedBy = "id.studyGroupId")
    @Builder.Default
    private List<StudyStep> steps = new ArrayList<>();

    @OneToMany(mappedBy = "id.studyGroup")
    @Builder.Default
    private List<Dibs> dibsList = new ArrayList<>();


    public void canUpdateBy(UserDetails user) {
        if (user.getUser().getRole().equals(Role.ADMIN)) {
            return;
        }

        if (this.owner.getId().equals(user.getUser().getId())) {
            return;
        }

        throw new RestException(ErrorCode.AUTH_FORBIDDEN);

    }
}
