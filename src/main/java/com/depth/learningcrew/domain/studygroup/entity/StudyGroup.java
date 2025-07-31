package com.depth.learningcrew.domain.studygroup.entity;

import java.time.LocalDate;
import java.util.List;

import com.depth.learningcrew.common.auditor.TimeStampedEntity;
import com.depth.learningcrew.domain.file.entity.StudyGroupImage;
import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
    private List<GroupCategory> categories;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "owner_id")
    private User owner;

    @OneToOne(mappedBy = "studyGroup")
    private StudyGroupImage studyGroupImage;

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
