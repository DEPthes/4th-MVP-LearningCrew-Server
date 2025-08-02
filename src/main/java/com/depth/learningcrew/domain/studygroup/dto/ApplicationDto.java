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

    public static ApplicationResponse from(Application application) {
      return ApplicationResponse.builder()
          .user(UserDto.UserResponse.from(application.getId().getUser()))
          .studyGroup(StudyGroupDto.StudyGroupResponse.from(application.getId().getStudyGroup(), false))
          .createdAt(application.getCreatedAt())
          .lastModifiedAt(application.getLastModifiedAt())
          .approvedAt(application.getApprovedAt())
          .state(application.getState())
          .build();
    }
  }
}