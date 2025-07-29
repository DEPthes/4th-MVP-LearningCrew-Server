package com.depth.learningcrew.domain.studygroup.dto;

import com.depth.learningcrew.domain.studygroup.entity.GroupCategory;
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
        private Integer id;
        private String name;

        public static GroupCategoryResponse from(GroupCategory groupCategory) {
            return GroupCategoryResponse.builder()
                    .id(groupCategory.getId())
                    .name(groupCategory.getName())
                    .build();
        }
    }
}
