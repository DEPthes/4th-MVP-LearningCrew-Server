package com.depth.learningcrew.domain.studygroup.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "STUDY_STEP")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyStep {

    @EmbeddedId
    private StudyStepId id;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "title", length = 255)
    private String title;

    @Lob
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

}
