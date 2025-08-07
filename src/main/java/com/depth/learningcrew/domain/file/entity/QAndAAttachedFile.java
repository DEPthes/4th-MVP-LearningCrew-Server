package com.depth.learningcrew.domain.file.entity;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.depth.learningcrew.domain.qna.entity.QAndA;

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
@DiscriminatorValue("QANDA_ATTACHED_FILE")
public class QAndAAttachedFile extends AttachedFile {
  @ManyToOne(fetch = FetchType.LAZY)
  private QAndA qAndA;

  public static QAndAAttachedFile from(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return null;
    }

    return QAndAAttachedFile.builder()
        .uuid(UUID.randomUUID().toString())
        .handlingType(HandlingType.DOWNLOADABLE)
        .fileName(file.getOriginalFilename())
        .size(file.getSize())
        .build();
  }
}
