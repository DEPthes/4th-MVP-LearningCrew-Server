package com.depth.learningcrew.domain.studygroup.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class StudyStepId implements Serializable {

    private Integer step;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "study_group_id")
    private StudyGroup studyGroupId;
}