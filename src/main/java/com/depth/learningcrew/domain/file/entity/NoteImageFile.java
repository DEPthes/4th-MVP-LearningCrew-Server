package com.depth.learningcrew.domain.file.entity;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.depth.learningcrew.domain.note.entity.Note;

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
@DiscriminatorValue("NOTE_IMAGE_FILE")
public class NoteImageFile extends AttachedFile {
  @ManyToOne(fetch = FetchType.LAZY)
  private Note note;

  public static NoteImageFile from(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return null;
    }

    return NoteImageFile.builder()
        .uuid(UUID.randomUUID().toString())
        .handlingType(HandlingType.IMAGE)
        .fileName(file.getOriginalFilename())
        .size(file.getSize())
        .build();
  }
}
