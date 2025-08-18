package com.depth.learningcrew.domain.file.entity;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

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

@Entity
@DiscriminatorValue("STEP_FILE")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StudyStepFile extends AttachedFile {
    @ManyToOne(fetch = FetchType.LAZY)
    private StudyStep studyStep;

    public static StudyStepFile from(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        return StudyStepFile.builder()
                .uuid(UUID.randomUUID().toString())
                .handlingType(HandlingType.DOWNLOADABLE)
                .fileName(file.getOriginalFilename())
                .size(file.getSize())
                .build();
    }
}
