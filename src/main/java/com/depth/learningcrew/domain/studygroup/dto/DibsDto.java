package com.depth.learningcrew.domain.studygroup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DibsDto {

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class DibsResponse {
        private Boolean dibs;

        public static DibsResponse from(Boolean dibs) {
            return DibsResponse.builder()
                    .dibs(dibs)
                    .build();
        }
    }

}
