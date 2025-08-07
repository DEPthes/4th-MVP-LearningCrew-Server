package com.depth.learningcrew.domain.quiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "QUIZ_OPTION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizOption {

  @EmbeddedId
  private QuizOptionId id;

  @Column(nullable = false, length = 255)
  private String content;

  @Column(nullable = false)
  private Boolean isAnswer;
}
