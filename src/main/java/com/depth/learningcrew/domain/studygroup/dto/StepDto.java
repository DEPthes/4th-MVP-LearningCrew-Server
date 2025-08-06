package com.depth.learningcrew.domain.studygroup.dto;

import com.depth.learningcrew.domain.studygroup.entity.StudyStep;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class StepDto {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Schema(description = "스터디 스텝 응답")
    public static class StepResponse {
        @Schema(description = "스텝 번호", example = "1")
        private Integer step;

        @Schema(description = "스텝 종료 날짜", example = "2025-08-15")
        private LocalDate endDate;

        public static StepResponse from(StudyStep step) {
            return StepResponse.builder()
                    .step(step.getId().getStep())
                    .endDate(step.getEndDate())
                    .build();
        }
    }
}