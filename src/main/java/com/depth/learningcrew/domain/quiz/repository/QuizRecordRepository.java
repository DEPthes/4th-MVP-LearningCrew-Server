package com.depth.learningcrew.domain.quiz.repository;

import com.depth.learningcrew.domain.quiz.entity.QuizRecord;
import com.depth.learningcrew.domain.quiz.entity.QuizRecordId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRecordRepository extends JpaRepository<QuizRecord, QuizRecordId> {
}
