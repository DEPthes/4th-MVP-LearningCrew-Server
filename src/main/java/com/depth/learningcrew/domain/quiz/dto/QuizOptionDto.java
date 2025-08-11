package com.depth.learningcrew.domain.quiz.dto;

import com.depth.learningcrew.domain.quiz.entity.Quiz;
import com.depth.learningcrew.domain.quiz.entity.QuizOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class QuizOptionDto {

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class QuizOptionResponse {
        private Long quiz;

        private Integer optionNum;

        private String content;

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
