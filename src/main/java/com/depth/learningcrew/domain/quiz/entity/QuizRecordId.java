package com.depth.learningcrew.domain.quiz.entity;

import java.io.Serializable;

import com.depth.learningcrew.domain.user.entity.User;

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
public class QuizRecordId implements Serializable {

  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  private Quiz quiz;

  public static QuizRecordId of(User user, Quiz quiz) {
    return QuizRecordId.builder()
        .user(user)
        .quiz(quiz)
        .build();
  }
}
