package com.depth.learningcrew.domain.quiz.dto;

import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class QuizRecordDto {

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class QuizSubmitRequest {
        private List<Answer> answers;

        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @Getter
        public static class Answer {
            private Long quizId;

            private List<Integer> selectedOptions;
        }
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class QuizSubmitResponse {
        private Long userId;

        private Integer step;

        private Integer totalQuizCount;

        private Integer correctCount;

        private Integer score;

        @JsonIgnore
        private LocalDateTime createdAt;

        public static QuizSubmitResponse from(User user, StudyGroup studyGroup, int total, int correct, LocalDateTime now) {
            return QuizSubmitResponse.builder()
                    .userId(user.getId())
                    .step(studyGroup.getCurrentStep())
                    .totalQuizCount(total)
                    .correctCount(correct)
                    .score(total == 0 ? 0 : Math.round((float) correct * 100 / total))
                    .createdAt(now)
                    .build();
        }
    }
}
