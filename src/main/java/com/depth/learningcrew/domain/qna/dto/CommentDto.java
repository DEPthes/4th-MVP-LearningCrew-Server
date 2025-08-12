package com.depth.learningcrew.domain.qna.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.depth.learningcrew.domain.file.dto.FileDto;
import com.depth.learningcrew.domain.qna.entity.Comment;
import com.depth.learningcrew.domain.user.dto.UserDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CommentDto {

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  @Schema(description = "댓글 생성 요청 DTO")
  public static class CommentCreateRequest {
    @Schema(description = "댓글 내용", example = "이 질문에 대한 답변입니다.", required = true)
    @NotBlank(message = "내용은 필수 입력값입니다.")
    private String content;

    @Schema(description = "첨부 이미지 목록")
    private List<MultipartFile> attachedImages;

    @Schema(description = "첨부 파일 목록")
    private List<MultipartFile> attachedFiles;

    public Comment toEntity() {
      return Comment.builder()
          .content(content)
          .build();
    }
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  @Schema(description = "댓글 수정 요청 DTO")
  public static class CommentUpdateRequest {
    @Schema(description = "댓글 내용", example = "수정된 답변 내용")
    private String content;

    @Schema(description = "새로 추가할 첨부 이미지 목록")
    private List<MultipartFile> newAttachedImages;

    @Schema(description = "새로 추가할 첨부 파일 목록")
    private List<MultipartFile> newAttachedFiles;

    @Schema(description = "삭제할 첨부 이미지 ID 목록")
    private List<String> deletedAttachedImages;

    @Schema(description = "삭제할 첨부 파일 ID 목록")
    private List<String> deletedAttachedFiles;
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  public static class CommentResponse {
    private Long id;
    private String content;
    private List<FileDto.FileResponse> attachedImages;
    private List<FileDto.FileResponse> attachedFiles;
    private UserDto.UserResponse createdBy;
    private UserDto.UserResponse lastModifiedBy;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime lastModifiedAt;

    public static CommentResponse from(Comment entity) {
      return CommentResponse.builder()
          .id(entity.getId())
          .content(entity.getContent())
          .attachedImages(entity.getAttachedImages().stream().map(FileDto.FileResponse::from).toList())
          .attachedFiles(entity.getAttachedFiles().stream().map(FileDto.FileResponse::from).toList())
          .createdBy(UserDto.UserResponse.from(entity.getCreatedBy()))
          .lastModifiedBy(UserDto.UserResponse.from(entity.getLastModifiedBy()))
          .createdAt(entity.getCreatedAt())
          .lastModifiedAt(entity.getLastModifiedAt())
          .build();
    }
  }

  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Data
  @Schema(description = "Comment 목록 조회 검색 조건")
  public static class SearchConditions {
    @Builder.Default
    @Schema(description = "정렬 기준", example = "created_at", allowableValues = { "created_at", "alphabet" })
    private String sort = "created_at";

    @Builder.Default
    @Schema(description = "정렬 순서", example = "desc", allowableValues = { "asc", "desc" })
    private String order = "asc";
  }
}
