package com.depth.learningcrew.domain.quiz.entity;

import java.util.ArrayList;
import java.util.List;

import com.depth.learningcrew.common.auditor.TimeStampedEntity;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.entity.StudyStep;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "QUIZ")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Quiz extends TimeStampedEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  private Long id;

  @Lob
  @Column
  private String quiz;

  @Column(nullable = false)
  private Integer step;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false, name = "study_group_id")
  private StudyGroup studyGroup;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumns({
          @JoinColumn(name = "step", referencedColumnName = "step", insertable = false, updatable = false),
          @JoinColumn(name = "study_group_id", referencedColumnName = "study_group_id", insertable = false, updatable = false)
  })
  private StudyStep studyStep;

  @OneToMany(mappedBy = "id.quiz", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<QuizOption> quizOptions = new ArrayList<>();

  @OneToMany(mappedBy = "id.quiz", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<QuizRecord> quizRecords = new ArrayList<>();

  public void addQuizOption(QuizOption quizOption) {
    this.quizOptions.add(quizOption);
  }

  public void removeQuizOption(QuizOption quizOption) {
    this.quizOptions.remove(quizOption);
  }

  public void addQuizRecord(QuizRecord quizRecord) {
    this.quizRecords.add(quizRecord);
  }

  public void removeQuizRecord(QuizRecord quizRecord) {
    this.quizRecords.remove(quizRecord);
  }
}
