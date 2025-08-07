package com.depth.learningcrew.domain.qna.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.depth.learningcrew.domain.file.dto.FileDto;
import com.depth.learningcrew.domain.qna.entity.QAndA;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class QAndADto {

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  @Schema(description = "질문 생성 요청 DTO")
  public static class QAndACreateRequest {
    @Schema(description = "질문 제목", example = "스프링 부트 설정 관련 질문입니다", required = true)
    @NotBlank
    private String title;

    @Schema(description = "질문 내용", example = "스프링 부트에서 JPA 설정을 어떻게 해야 하나요?", required = true)
    @NotBlank
    private String content;

    @Schema(description = "첨부 파일 목록", example = "[]")
    private List<MultipartFile> attachedFiles;

    @Schema(description = "첨부 이미지 목록", example = "[]")
    private List<MultipartFile> attachedImages;

    public QAndA toEntity() {
      return QAndA.builder()
          .title(title)
          .content(content)
          .build();
    }
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  @Schema(description = "질문 목록 조회 응답 DTO")
  public static class QAndAResponse {
    @Schema(description = "질문 ID", example = "42")
    private Long id;

    @Schema(description = "스터디 스텝 번호", example = "1")
    private Integer step;

    @Schema(description = "질문 제목", example = "스프링 부트 설정 관련 질문입니다")
    private String title;

    @Schema(description = "첨부 파일 개수", example = "2")
    private Integer attachedFiles;

    @Schema(description = "첨부 이미지 개수", example = "1")
    private Integer attachedImages;

    public static QAndAResponse from(QAndA entity) {
      return QAndAResponse.builder()
          .id(entity.getId())
          .step(entity.getStep())
          .title(entity.getTitle())
          .attachedFiles(entity.getAttachedFiles().size())
          .attachedImages(entity.getAttachedImages().size())
          .build();
    }
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  @Schema(description = "질문 상세 조회 응답 DTO")
  public static class QAndADetailResponse {
    @Schema(description = "질문 ID", example = "42")
    private Long id;

    @Schema(description = "스터디 스텝 번호", example = "1")
    private Integer step;

    @Schema(description = "질문 제목", example = "스프링 부트 설정 관련 질문입니다")
    private String title;

    @Schema(description = "질문 내용", example = "스프링 부트에서 JPA 설정을 어떻게 해야 하나요?")
    private String content;

    @Schema(description = "댓글 목록")
    private List<CommentDto.CommentResponse> comments;

    @Schema(description = "첨부 파일 목록")
    private List<FileDto.FileResponse> attachedFiles;

    @Schema(description = "첨부 이미지 목록")
    private List<FileDto.FileResponse> attachedImages;

    public static QAndADetailResponse from(QAndA entity) {
      return QAndADetailResponse.builder()
          .id(entity.getId())
          .step(entity.getStep())
          .title(entity.getTitle())
          .content(entity.getContent())
          .comments(entity.getComments().stream().map(CommentDto.CommentResponse::from).toList())
          .attachedFiles(entity.getAttachedFiles().stream().map(FileDto.FileResponse::from).toList())
          .attachedImages(entity.getAttachedImages().stream().map(FileDto.FileResponse::from).toList())
          .build();
    }
  }
}
