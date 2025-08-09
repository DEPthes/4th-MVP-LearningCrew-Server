package com.depth.learningcrew.domain.file.entity;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.depth.learningcrew.domain.qna.entity.Comment;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@DiscriminatorValue("COMMENT_ATTACHED_FILE")
public class CommentAttachedFile extends AttachedFile {
  @ManyToOne(fetch = FetchType.LAZY)
  private Comment comment;

  public static CommentAttachedFile from(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return null;
    }

    return CommentAttachedFile.builder()
        .uuid(UUID.randomUUID().toString())
        .handlingType(HandlingType.DOWNLOADABLE)
        .fileName(file.getOriginalFilename())
        .size(file.getSize())
        .build();
  }
}
