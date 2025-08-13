package com.depth.learningcrew.domain.qna.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.depth.learningcrew.domain.file.dto.FileDto;
import com.depth.learningcrew.domain.qna.entity.QAndA;
import com.depth.learningcrew.domain.user.dto.UserDto;

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
  @Schema(description = "질문 수정 요청 DTO")
  public static class QAndAUpdateRequest {
    @Schema(description = "질문 제목", example = "수정된 질문 제목")
    private String title;

    @Schema(description = "질문 내용", example = "수정된 질문 내용")
    private String content;

    @Schema(description = "새로 추가할 첨부 파일 목록")
    private List<MultipartFile> newAttachedFiles;

    @Schema(description = "새로 추가할 첨부 이미지 목록")
    private List<MultipartFile> newAttachedImages;

    @Schema(description = "삭제할 첨부 파일 ID 목록")
    private List<String> deletedAttachedFiles;

    @Schema(description = "삭제할 이미지 파일 ID 목록")
    private List<String> deletedAttachedImages;
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

    @Schema(description = "답변 개수", example = "3")
    private int commentCount;

    @Schema(description = "생성일시", example = "2025-08-06T20:00:00Z")
    private String createdAt;

    @Schema(description = "수정일시", example = "2025-08-07T10:30:00Z")
    private String lastModifiedAt;

    @Schema(description = "작성자 정보")
    private UserDto.UserResponse createdBy;

    @Schema(description = "수정자 정보")
    private UserDto.UserResponse lastModifiedBy;

    @Schema(description = "첨부된 이미지")
    private List<FileDto.FileResponse> attachedImages;

    @Schema(description = "첨부된 파일")
    private List<FileDto.FileResponse> attachedFiles;

    public static QAndAResponse from(QAndA entity, int commentCount) {
      return QAndAResponse.builder()
          .id(entity.getId())
          .step(entity.getStep())
          .title(entity.getTitle())
          .commentCount(commentCount)
          .attachedImages(entity.getAttachedImages().stream().map(FileDto.FileResponse::from).toList())
          .attachedFiles(entity.getAttachedFiles().stream().map(FileDto.FileResponse::from).toList())
          .createdBy(UserDto.UserResponse.from(entity.getCreatedBy()))
          .lastModifiedBy(UserDto.UserResponse.from(entity.getLastModifiedBy()))
          .createdAt(entity.getCreatedAt().toString())
          .lastModifiedAt(entity.getLastModifiedAt().toString())
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

    @Schema(description = "작성자 정보")
    private UserDto.UserResponse createdBy;

    @Schema(description = "수정자 정보")
    private UserDto.UserResponse lastModifiedBy;

    @Schema(description = "생성일시", example = "2025-08-06T20:00:00Z")
    private String createdAt;

    @Schema(description = "수정일시", example = "2025-08-07T10:30:00Z")
    private String lastModifiedAt;

    @Schema(description = "답변 갯수", example = "5")
    private Long commentCount;

    private static QAndADetailResponse.QAndADetailResponseBuilder baseBuilder(QAndA e) {
      return QAndADetailResponse.builder()
              .id(e.getId())
              .step(e.getStep())
              .title(e.getTitle())
              .content(e.getContent())
              .comments(e.getComments().stream().map(CommentDto.CommentResponse::from).toList())
              .attachedFiles(e.getAttachedFiles().stream().map(FileDto.FileResponse::from).toList())
              .attachedImages(e.getAttachedImages().stream().map(FileDto.FileResponse::from).toList())
              .createdBy(UserDto.UserResponse.from(e.getCreatedBy()))
              .lastModifiedBy(UserDto.UserResponse.from(e.getLastModifiedBy()))
              .createdAt(e.getCreatedAt().toString())
              .lastModifiedAt(e.getLastModifiedAt().toString());
    }

    public static QAndADetailResponse from(QAndA entity) {
      return baseBuilder(entity).build();
    }

    public static QAndADetailResponse of(QAndA entity, Long commentCount) {
      return baseBuilder(entity)
              .commentCount(commentCount)
              .build();
    }
  }

  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Data
  @Schema(description = "Q&A 목록 조회 검색 조건")
  public static class SearchConditions {
    @Builder.Default
    @Schema(description = "정렬 기준", example = "created_at", allowableValues = { "created_at", "alphabet" })
    private String sort = "created_at";

    @Builder.Default
    @Schema(description = "정렬 순서", example = "desc", allowableValues = { "asc", "desc" })
    private String order = "asc";

    @Schema(description = "스텝", example = "1")
    private Integer step;

    @Schema(description = "검색어", example = "스터디")
    private String searchKeyword;
  }
}
