package com.depth.learningcrew.domain.quiz.repository;

import com.depth.learningcrew.domain.quiz.entity.QuizOption;
import com.depth.learningcrew.domain.quiz.entity.QuizOptionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizOptionRepository extends JpaRepository<QuizOption, QuizOptionId> {
}
