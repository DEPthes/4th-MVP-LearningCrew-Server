package com.depth.learningcrew.domain.quiz.entity;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class QuizOptionId implements Serializable {

  @ManyToOne(fetch = FetchType.LAZY)
  private Quiz quiz;

  private Integer optionNum;

  public static QuizOptionId of(Quiz quiz, Integer optionNum) {
    return QuizOptionId.builder()
        .quiz(quiz)
        .optionNum(optionNum)
        .build();
  }
}
