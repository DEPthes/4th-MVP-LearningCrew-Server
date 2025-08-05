package com.depth.learningcrew.domain.studygroup.dto;

import java.time.LocalDateTime;

import com.depth.learningcrew.domain.studygroup.entity.Member;
import com.depth.learningcrew.domain.user.dto.UserDto.UserResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MemberDto {

  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Data
  @Schema(description = "멤버 검색 조건")
  public static class SearchConditions {
    @Schema(description = "정렬 기준", example = "created_at", allowableValues = { "created_at", "alphabet" })
    private String sort;

    @Schema(description = "정렬 순서", example = "desc", allowableValues = { "asc", "desc" })
    private String order;
  }

  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Data
  @Schema(description = "멤버 정보 응답")
  public static class MemberResponse {
    @Schema(description = "사용자 정보")
    private UserResponse user;

    @Schema(description = "생성일시", example = "2024-01-01T00:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-01T00:00:00")
    private LocalDateTime lastModifiedAt;

    public static MemberResponse from(Member member) {
      return MemberResponse.builder()
          .user(UserResponse.from(member.getId().getUser()))
          .createdAt(member.getCreatedAt())
          .lastModifiedAt(member.getLastModifiedAt())
          .build();
    }
  }
}
