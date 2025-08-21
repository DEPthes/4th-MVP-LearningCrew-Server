package com.depth.learningcrew.domain.studygroup.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.depth.learningcrew.common.auditor.TimeStampedEntity;
import com.depth.learningcrew.common.entitybase.CleanableEntity;
import com.depth.learningcrew.domain.file.entity.StudyGroupImage;
import com.depth.learningcrew.domain.file.handler.FileHandler;
import com.depth.learningcrew.domain.note.entity.Note;
import com.depth.learningcrew.domain.qna.entity.QAndA;
import com.depth.learningcrew.domain.quiz.entity.Quiz;
import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
public class StudyGroup extends TimeStampedEntity implements CleanableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, length = 255)
    private String summary;

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

    @OneToOne(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private StudyGroupImage studyGroupImage;

    @OneToMany(mappedBy = "id.studyGroupId", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StudyStep> steps = new ArrayList<>();

    @OneToMany(mappedBy = "id.studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Dibs> dibsList = new ArrayList<>();

    @OneToMany(mappedBy = "id.studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Member> members = new ArrayList<>();

    @OneToMany(mappedBy = "id.studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Application> applications = new ArrayList<>();

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Note> notes = new ArrayList<>();

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Quiz> quizzes = new ArrayList<>();

    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QAndA> qAndAs = new ArrayList<>();

    public void canUpdateBy(UserDetails user) {
        if (user.getUser().getRole().equals(Role.ADMIN)) {
            return;
        }

        if (this.owner.getId().equals(user.getUser().getId())) {
            return;
        }

        throw new RestException(ErrorCode.AUTH_FORBIDDEN);

    }

    public void canDeleteBy(UserDetails user) {
        if (user.getUser().getRole().equals(Role.ADMIN)) {
            return;
        }

        if (this.owner.getId().equals(user.getUser().getId())) {
            return;
        }

        throw new RestException(ErrorCode.AUTH_FORBIDDEN);
    }

    public void addCategory(GroupCategory category) {
        if (!this.categories.contains(category)) {
            this.categories.add(category);
            category.getStudyGroups().add(this);
        }
    }

    public void decreaseMemberCount() {
        if (this.memberCount > 0) {
            this.memberCount--;
        }
    }

    public void cleanup(FileHandler fileHandler) {
        // clean study group image
        if (this.studyGroupImage != null) {
            fileHandler.deleteFile(this.studyGroupImage);
        }

        // clean step files
        this.steps.forEach(step -> step.cleanRelavantFiles(fileHandler));

        // clean note files
        this.notes.forEach(note -> note.cleanup(fileHandler));

        // clean q&a files
        this.qAndAs.forEach(qAndA -> qAndA.cleanup(fileHandler));
    }
}
