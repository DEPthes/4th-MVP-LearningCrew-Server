package com.depth.learningcrew.domain.studygroup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "STUDY_STEP")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyStep {

    @EmbeddedId
    private StudyStepId id;

    @Column(name="end_date", nullable = false)
    private LocalDate endDate;

}
