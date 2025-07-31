package com.depth.learningcrew.domain.studygroup.dto;

import com.depth.learningcrew.domain.studygroup.entity.GroupCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class GroupCategoryDto {

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class GroupCategoryResponse {
        @Schema(description = "그룹 카테고리 ID", example = "1")
        private Integer id;
        @Schema(description = "그룹 카테고리명", example = "언어")
        private String name;

        public static GroupCategoryResponse from(GroupCategory groupCategory) {
            return GroupCategoryResponse.builder()
                    .id(groupCategory.getId())
                    .name(groupCategory.getName())
                    .build();
        }
    }
}
