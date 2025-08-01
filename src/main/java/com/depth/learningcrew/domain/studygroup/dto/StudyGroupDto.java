package com.depth.learningcrew.domain.studygroup.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.depth.learningcrew.domain.file.dto.FileDto;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.user.dto.UserDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class StudyGroupDto {

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Schema(description = "내 주최 그룹 목록 조회 검색 조건")
    public static class SearchConditions {
        @Schema(description = "정렬 기준", example = "created_at", allowableValues = { "created_at", "relative",
                "alphabet" })
        private String sort = "created_at";

        @Schema(description = "정렬 순서", example = "desc", allowableValues = { "asc", "desc" })
        private String order = "desc";

        @Schema(description = "카테고리 ID", example = "1")
        private Integer categoryId;

        @Schema(description = "검색어", example = "스터디")
        private String searchKeyword;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Schema(description = "내 주최 그룹 목록 응답")
    public static class StudyGroupPaginationResponse {
        @Schema(description = "스터디 그룹 ID", example = "123")
        private Integer id;

        @Schema(description = "스터디 그룹 이름", example = "group name")
        private String name;

        @Schema(description = "스터디 그룹 요약", example = "group summary")
        private String summary;

        @Schema(description = "최대 멤버 수", example = "10")
        private Integer maxMembers;

        @Schema(description = "그룹 이미지 정보")
        private FileDto.FileResponse groupImage;

        @Schema(description = "카테고리 목록")
        private List<GroupCategoryDto.GroupCategoryResponse> categories;

        @Schema(description = "현재 멤버 수", example = "3")
        private Integer memberCount;

        @Schema(description = "찜 여부", example = "true")
        private Boolean dibs;

        @Schema(description = "시작 날짜", example = "2024-01-01")
        private LocalDate startDate;

        @Schema(description = "종료 날짜", example = "2024-12-31")
        private LocalDate endDate;

        @Schema(description = "그룹 주최자 정보")
        private UserDto.UserResponse owner;

        @Schema(description = "생성 시간", example = "2024-01-01T00:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "마지막 수정 시간", example = "2024-01-01T00:00:00")
        private LocalDateTime lastModifiedAt;

        public static StudyGroupPaginationResponse of(StudyGroup studyGroup, Boolean dibs) {
            return StudyGroupPaginationResponse.builder()
                    .id(studyGroup.getId())
                    .name(studyGroup.getName())
                    .summary(studyGroup.getSummary())
                    .maxMembers(studyGroup.getMaxMembers())
                    .groupImage(FileDto.FileResponse.from(studyGroup.getStudyGroupImage()))
                    .categories(studyGroup.getCategories().stream()
                            .map(GroupCategoryDto.GroupCategoryResponse::from)
                            .collect(Collectors.toList()))
                    .memberCount(studyGroup.getMemberCount())
                    .dibs(dibs)
                    .startDate(studyGroup.getStartDate())
                    .endDate(studyGroup.getEndDate())
                    .owner(UserDto.UserResponse.from(studyGroup.getOwner()))
                    .createdAt(studyGroup.getCreatedAt())
                    .lastModifiedAt(studyGroup.getLastModifiedAt())
                    .build();
        }
    }
    
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Schema(description = "스터디 그룹 상세 조회 응답")
    public static class StudyGroupDetailResponse {
        @Schema(description = "스터디 그룹 ID", example = "123")
        private Integer id;

        @Schema(description = "스터디 그룹 이름", example = "group name")
        private String name;

        @Schema(description = "스터디 그룹 요약", example = "group summary")
        private String summary;

        @Schema(description = "최대 모집 인원 수", example = "10")
        private Integer maxMembers;

        @Schema(description = "그룹 이미지 정보")
        private FileDto.FileResponse groupImage;

        @Schema(description = "카테고리 목록")
        private List<GroupCategoryDto.GroupCategoryResponse> categories;

        @Schema(description = "현재 멤버 수", example = "3")
        private Integer memberCount;

        @Schema(description = "찜 여부", example = "true")
        private Boolean dibs;

        @Schema(description = "시작 날짜", example = "2024-01-01")
        private LocalDate startDate;

        @Schema(description = "종료 날짜", example = "2024-12-31")
        private LocalDate endDate;

        @Schema(description = "그룹 주최자 정보")
        private UserDto.UserResponse owner;

        @Schema(description = "생성 시간", example = "2024-01-01T00:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "마지막 수정 시간", example = "2024-01-01T00:00:00")
        private LocalDateTime lastModifiedAt;

        @Schema(description = "스터디 내용", example = "스터디 내용")
        private String content;

        @Schema(description = "스터디 스텝(진도) 목록")
        private List<StepDto.StepResponse> steps;

        @Schema(description = "현재 진행 중인 스텝", example = "1")
        private Integer currentStep;

        public static StudyGroupDetailResponse from(StudyGroup studyGroup, Boolean dibs) {
            return StudyGroupDetailResponse.builder()
                    .id(studyGroup.getId())
                    .name(studyGroup.getName())
                    .summary(studyGroup.getSummary())
                    .maxMembers(studyGroup.getMaxMembers())
                    .groupImage(FileDto.FileResponse.from(studyGroup.getStudyGroupImage()))
                    .categories(studyGroup.getCategories().stream()
                            .map(GroupCategoryDto.GroupCategoryResponse::from)
                            .collect(Collectors.toList()))
                    .memberCount(studyGroup.getMemberCount())
                    .dibs(dibs)
                    .startDate(studyGroup.getStartDate())
                    .endDate(studyGroup.getEndDate())
                    .owner(UserDto.UserResponse.from(studyGroup.getOwner()))
                    .createdAt(studyGroup.getCreatedAt())
                    .lastModifiedAt(studyGroup.getLastModifiedAt())
                    .content(studyGroup.getContent())
                    .steps(
                            studyGroup.getSteps().stream()
                                    .map(StepDto.StepResponse::from)
                                    .collect(Collectors.toList())
                    )
                    .currentStep(studyGroup.getCurrentStep())
                    .build();
        }
        }
}