package com.depth.learningcrew.domain.file.entity;

import com.depth.learningcrew.domain.studygroup.entity.StudyStep;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Entity
@DiscriminatorValue("STEP_IMAGE")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StudyStepImage extends AttachedFile {
    @ManyToOne(fetch = FetchType.LAZY)
    private StudyStep studyStep;

    public static StudyStepImage from(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        return StudyStepImage.builder()
                .uuid(UUID.randomUUID().toString())
                .handlingType(HandlingType.IMAGE)
                .fileName(file.getOriginalFilename())
                .size(file.getSize())
                .build();
    }
}
