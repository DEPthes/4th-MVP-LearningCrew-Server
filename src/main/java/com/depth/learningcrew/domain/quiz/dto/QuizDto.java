package com.depth.learningcrew.domain.quiz.dto;

import com.depth.learningcrew.domain.quiz.entity.Quiz;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class QuizDto {

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class QuizResponse {
        private Long id;

        private String quiz;

        private Integer step;

        private LocalDateTime createdAt;

        private List<QuizOptionDto.QuizOptionResponse> options;

        public static QuizResponse from(Quiz quiz) {
            return QuizResponse.builder()
                    .id(quiz.getId())
                    .quiz(quiz.getQuiz())
                    .step(quiz.getStep())
                    .createdAt(quiz.getCreatedAt())
                    .options(quiz.getQuizOptions().stream()
                            .map(QuizOptionDto.QuizOptionResponse::from)
                            .collect(Collectors.toList()))
                    .build();
        }
    }
}
