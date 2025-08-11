package com.depth.learningcrew.domain.quiz.repository;

import com.depth.learningcrew.domain.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    boolean existsByStudyGroup_IdAndStepAndCreatedAtBetween(
            Long studyGroupId, Integer step,
            LocalDateTime startInclusive, LocalDateTime endExclusive
    );
}
