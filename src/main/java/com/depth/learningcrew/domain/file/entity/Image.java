package com.depth.learningcrew.domain.file.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Entity
@DiscriminatorValue("IMAGE")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class Image extends AttachedFile {

    @Column(name = "image_id")
    private Long id;

    public static Image from(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        return Image.builder()
                .uuid(UUID.randomUUID().toString())
                .handlingType(HandlingType.IMAGE)
                .fileName(file.getOriginalFilename())
                .size(file.getSize())
                .build();
    }
}
