package com.depth.learningcrew.domain.studygroup.dto;

import com.depth.learningcrew.domain.studygroup.entity.StudyStep;
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
    public static class StepResponse {

        private Integer step;
        private LocalDate endDate;

        public static StepResponse from(StudyStep step) {
            return StepResponse.builder()
                    .step(step.getId().getStep())
                    .endDate(step.getEndDate())
                    .build();
        }
    }
}