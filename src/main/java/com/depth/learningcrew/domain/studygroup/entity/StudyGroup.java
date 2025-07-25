package com.depth.learningcrew.domain.studygroup.entity;

import com.depth.learningcrew.common.auditor.TimeStampedEntity;
import com.depth.learningcrew.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class StudyGroup extends TimeStampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(length = 255)
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

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "owner_id")
    private User owner;
}
