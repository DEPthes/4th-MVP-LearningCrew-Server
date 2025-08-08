package com.depth.learningcrew.domain.studygroup.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

import com.depth.learningcrew.domain.file.dto.FileDto;
import com.depth.learningcrew.domain.studygroup.entity.GroupCategory;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.user.dto.UserDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class StudyGroupDto {

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Schema(description = "내 주최 그룹 목록 조회 검색 조건")
    public static class SearchConditions {
        @Builder.Default
        @Schema(description = "정렬 기준", example = "created_at", allowableValues = { "created_at", "relative",
                "alphabet" })
        private String sort = "created_at";

        @Builder.Default
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
    public static class StudyGroupResponse {
        @Schema(description = "스터디 그룹 ID", example = "123")
        private Long id;

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

        public static StudyGroupResponse from(StudyGroup studyGroup, Boolean dibs) {
            return StudyGroupResponse.builder()
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
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Schema(description = "스터디 그룹 정보 수정 요청")
    public static class StudyGroupUpdateRequest {
        @Schema(description = "스터디 그룹 이름", example = "수정된 그룹명")
        private String name;

        @Schema(description = "카테고리 이름 목록", example = "[\"언어\", \"IT\"]")
        private List<String> categories;

        @Schema(description = "스터디 그룹 요약", example = "수정된 요약")
        private String summary;

        @Schema(description = "시작 날짜", example = "2024-01-01")
        private LocalDate startDate;

        @Schema(description = "그룹 이미지 파일")
        private MultipartFile groupImage;

        public void applyTo(StudyGroup studyGroup, List<GroupCategory> categories) {
            if (name != null) {
                studyGroup.setName(name);
            }
            if (categories != null) {
                studyGroup.setCategories(categories);
            }
            if (summary != null) {
                studyGroup.setSummary(summary);
            }
            if (startDate != null) {
                studyGroup.setStartDate(startDate);
            }
        }
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Schema(description = "스터디 그룹 정보 수정 응답")
    public static class StudyGroupUpdateResponse {
        @Schema(description = "스터디 그룹 ID", example = "123")
        private Long id;

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

        public static StudyGroupUpdateResponse from(StudyGroup studyGroup) {
            return StudyGroupUpdateResponse.builder()
                    .id(studyGroup.getId())
                    .name(studyGroup.getName())
                    .summary(studyGroup.getSummary())
                    .maxMembers(studyGroup.getMaxMembers())
                    .groupImage(FileDto.FileResponse.from(studyGroup.getStudyGroupImage()))
                    .categories(studyGroup.getCategories().stream()
                            .map(GroupCategoryDto.GroupCategoryResponse::from)
                            .collect(Collectors.toList()))
                    .memberCount(studyGroup.getMemberCount())
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
        private Long id;

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
                                    .collect(Collectors.toList()))
                    .currentStep(studyGroup.getCurrentStep())
                    .build();
        }
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Schema(description = "스터디 그룹 생성 요청")
    public static class StudyGroupCreateRequest {
        @Schema(description = "스터디 그룹 이름", example = "group name")
        private String name;

        @Schema(description = "스터디 그룹 요약", example = "group summary")
        private String summary;

        @Schema(description = "최대 모집 인원 수", example = "10")
        private Integer maxMembers;

        @Schema(description = "그룹 이미지 파일", type = "string", format = "binary")
        private MultipartFile groupImage;

        @Schema(description = "카테고리 목록")
        private List<String> categories;

        @Schema(description = "시작 날짜", example = "2024-01-01")
        private LocalDate startDate;

        @Schema(description = "종료 날짜", example = "2024-12-31")
        private LocalDate endDate;

        @Schema(description = "각 단계 종료일 리스트")
        private List<LocalDate> steps;

        public StudyGroup toEntity() {
            return StudyGroup.builder()
                    .name(name)
                    .summary(summary)
                    .maxMembers(maxMembers)
                    .memberCount(1) // 초기 멤버 수는 1로 설정 (주최자 포함)
                    .currentStep(1) // 초기 스텝은 1로 설정
                    .startDate(startDate)
                    .endDate(endDate)
                    .content("") // 초기 내용은 빈 문자열
                    .build();
        }
    }

}