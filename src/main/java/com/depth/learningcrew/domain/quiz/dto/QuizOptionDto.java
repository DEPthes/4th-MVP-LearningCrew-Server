package com.depth.learningcrew.domain.quiz.dto;

import com.depth.learningcrew.domain.quiz.entity.QuizOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class QuizOptionDto {

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Schema(description = "퀴즈 옵션 목록 응답")
    public static class QuizOptionResponse {
        @Schema(description = "퀴즈 ID", example = "123")
        private Long quiz;

        @Schema(description = "옵션 번호", example = "1")
        private Integer optionNum;

        @Schema(description = "옵션 내용", example = "quiz option content")
        private String content;

        @Schema(description = "정답 여부", example = "true")
        private Boolean isAnswer;

        public static QuizOptionResponse from(QuizOption option) {
            return QuizOptionResponse.builder()
                    .quiz(option.getId().getQuiz().getId())
                    .optionNum(option.getId().getOptionNum())
                    .content(option.getContent())
                    .isAnswer(option.getIsAnswer())
                    .build();
        }
    }
}
