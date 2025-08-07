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
@DiscriminatorValue("QANDA_IMAGE_FILE")
public class QAndAImageFile extends AttachedFile {
  @ManyToOne(fetch = FetchType.LAZY)
  private QAndA qAndA;

  public static QAndAImageFile from(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return null;
    }

    return QAndAImageFile.builder()
        .uuid(UUID.randomUUID().toString())
        .handlingType(HandlingType.IMAGE)
        .fileName(file.getOriginalFilename())
        .size(file.getSize())
        .build();
  }
}
