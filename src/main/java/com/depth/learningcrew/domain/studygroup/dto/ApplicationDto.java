package com.depth.learningcrew.domain.studygroup.dto;

import java.time.LocalDateTime;

import com.depth.learningcrew.domain.studygroup.entity.Application;
import com.depth.learningcrew.domain.studygroup.entity.State;
import com.depth.learningcrew.domain.user.dto.UserDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ApplicationDto {

  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Schema(description = "스터디 그룹 가입 신청 응답")
  public static class ApplicationResponse {
    @Schema(description = "신청자 정보")
    private UserDto.UserResponse user;

    @Schema(description = "스터디 그룹 정보")
    private StudyGroupDto.StudyGroupResponse studyGroup;

    @Schema(description = "생성 시간", example = "2024-01-01T00:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "마지막 수정 시간", example = "2024-01-01T00:00:00")
    private LocalDateTime lastModifiedAt;

    @Schema(description = "수락 시간", example = "2024-01-01T00:00:00")
    private LocalDateTime approvedAt;

    @Schema(description = "신청 상태", example = "PENDING", allowableValues = { "PENDING", "APPROVED", "REJECTED" })
    private State state;

    public static ApplicationResponse from(Application application, Boolean dibs) {
      return ApplicationResponse.builder()
          .user(UserDto.UserResponse.from(application.getId().getUser()))
          .studyGroup(StudyGroupDto.StudyGroupResponse.from(application.getId().getStudyGroup(), dibs))
          .createdAt(application.getCreatedAt())
          .lastModifiedAt(application.getLastModifiedAt())
          .approvedAt(application.getApprovedAt())
          .state(application.getState())
          .build();
    }
  }

  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Schema(description = "스터디 그룹 가입 신청 검색 조건")
  public static class SearchConditions {
    @Schema(description = "검색 키워드 (신청자 이름 | 스터디 그룹 이름)", example = "홍길동 | 스터디 그룹 이름")
    private String keyword;

    @Schema(description = "신청 상태 필터", example = "PENDING", allowableValues = { "PENDING", "APPROVED", "REJECTED" })
    private State state;

    @Schema(description = "정렬 기준", example = "created_at", allowableValues = { "created_at", "alphabet" })
    @Builder.Default
    private String sort = "created_at";

    @Schema(description = "정렬 순서", example = "desc", allowableValues = { "asc", "desc" })
    @Builder.Default
    private String order = "desc";
  }
}