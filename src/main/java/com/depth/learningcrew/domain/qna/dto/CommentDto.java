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
  public static class CommentResponse {
    private Long id;
    private String content;
    private List<FileDto.FileResponse> attachedImages;
    private List<FileDto.FileResponse> attachedFiles;
    private UserDto.UserResponse createdBy;
    private UserDto.UserResponse lastModifiedBy;

    public static CommentResponse from(Comment entity) {
      return CommentResponse.builder()
          .id(entity.getId())
          .content(entity.getContent())
          .attachedImages(entity.getAttachedImages().stream().map(FileDto.FileResponse::from).toList())
          .attachedFiles(entity.getAttachedFiles().stream().map(FileDto.FileResponse::from).toList())
          .createdBy(UserDto.UserResponse.from(entity.getCreatedBy()))
          .lastModifiedBy(UserDto.UserResponse.from(entity.getLastModifiedBy()))
          .build();
    }

  }

}
