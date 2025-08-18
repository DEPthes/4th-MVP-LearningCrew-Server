package com.depth.learningcrew.domain.studygroup.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.depth.learningcrew.domain.file.entity.StudyStepFile;
import com.depth.learningcrew.domain.file.entity.StudyStepImage;
import jakarta.persistence.*;
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

    @OneToMany(mappedBy = "studyStep", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StudyStepFile> files = new ArrayList<>();

    @OneToMany(mappedBy = "studyStep", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StudyStepImage> images = new ArrayList<>();

}
