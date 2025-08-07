package com.depth.learningcrew.domain.qna.dto;

import java.util.List;

import com.depth.learningcrew.domain.file.dto.FileDto;
import com.depth.learningcrew.domain.qna.entity.Comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CommentDto {

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  public static class CommentResponse {
    private Long id;
    private String content;
    private List<FileDto.FileResponse> attachedImages;

    public static CommentResponse from(Comment entity) {
      return CommentResponse.builder()
          .id(entity.getId())
          .content(entity.getContent())
          .attachedImages(entity.getAttachedImages().stream().map(FileDto.FileResponse::from).toList())
          .build();
    }

  }

}
