package com.depth.learningcrew.domain.quiz.dto;

import com.depth.learningcrew.domain.quiz.entity.QuizRecord;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class QuizRecordDto {

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Schema(description = "퀴즈 기록 검색 조건")
    public static class SearchConditions {
        @Schema(description = "정렬 기준", example = "step", allowableValues = { "created_at", "step" })
        private String sort;

        @Schema(description = "정렬 순서", example = "asc", allowableValues = { "asc", "desc" })
        private String order;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Schema(description = "퀴즈 제출 요청")
    public static class QuizSubmitRequest {
        private List<Answer> answers;

        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @Getter
        public static class Answer {
            @Schema(description = "퀴즈 ID", example = "123")
            private Long quizId;

            @Schema(description = "제출한 퀴즈 답변")
            private List<Integer> selectedOptions;
        }
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Schema(description = "퀴즈 제출 응답")
    public static class QuizSubmitResponse {
        @Schema(description = "유저 ID", example = "123")
        private Long userId;

        @Schema(description = "스터디 스텝(진도)", example = "1")
        private Integer step;

        @Schema(description = "전체 퀴즈 개수", example = "2")
        private Integer totalQuizCount;

        @Schema(description = "맞은 정답 개수", example = "1")
        private Integer correctCount;

        @Schema(description = "점수(correct * 100 / total)", example = "50")
        private Integer score;

        @Schema(description = "제출 생성 시간", example = "2024-01-01T00:00:00")
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

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Schema(description = "퀴즈 기록 정보 응답")
    public static class QuizRecordResponse {
        @Schema(description = "유저 ID", example = "123")
        private Long userId;

        @Schema(description = "스터디 스텝(진도)", example = "1")
        private Integer step;

        @Schema(description = "전체 퀴즈 개수", example = "2")
        private Integer totalQuizCount;

        @Schema(description = "맞은 정답 개수", example = "1")
        private Integer correctCount;

        @Schema(description = "점수(correct * 100 / total)", example = "50")
        private Integer score;

        public static QuizRecordResponse from(User user, StudyGroup studyGroup, int total, QuizRecord quizRecord) {
            return QuizRecordResponse.builder()
                    .userId(user.getId())
                    .step(studyGroup.getCurrentStep())
                    .totalQuizCount(total)
                    .correctCount(quizRecord.getCorrectCount())
                    .score(total == 0 ? 0 : Math.round((float) quizRecord.getCorrectCount() * 100 / total))
                    .build();
        }
    }
}
