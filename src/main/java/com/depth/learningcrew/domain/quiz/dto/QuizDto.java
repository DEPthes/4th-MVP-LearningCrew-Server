package com.depth.learningcrew.domain.quiz.dto;

import com.depth.learningcrew.domain.quiz.entity.Quiz;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "퀴즈 목록 응답")
    public static class QuizResponse {
        @Schema(description = "퀴즈 ID", example = "123")
        private Long id;

        @Schema(description = "퀴즈 내용", example = "quiz content")
        private String quiz;

        @Schema(description = "스터디 스텝(진도)", example = "1")
        private Integer step;

        @Schema(description = "생성 시간", example = "2024-01-01T00:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "퀴즈 옵션 목록")
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
