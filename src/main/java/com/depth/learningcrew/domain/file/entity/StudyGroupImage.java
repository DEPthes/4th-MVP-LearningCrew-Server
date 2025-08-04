package com.depth.learningcrew.domain.file.entity;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class StudyGroupImage extends AttachedFile {

    @OneToOne
    private StudyGroup studyGroup;

    public static StudyGroupImage from(MultipartFile file, StudyGroup studyGroup) {
        throwIfNotAImageFile(file);

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new RestException(ErrorCode.FILE_NOT_FOUND);
        }

        return StudyGroupImage.builder()
                .uuid(UUID.randomUUID().toString())
                .fileName(file.getOriginalFilename())
                .handlingType(HandlingType.IMAGE)
                .studyGroup(studyGroup)
                .size(file.getSize())
                .build();

    }

    private static void throwIfNotAImageFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String[] splitted = fileName.split("\\.");

        if (splitted.length < 1) {
            throw new RestException(ErrorCode.FILE_NOT_IMAGE);
        }

        String extension = splitted[splitted.length - 1];
        if (!List.of("JPG").contains(extension.toUpperCase())) {
            throw new RestException(ErrorCode.FILE_NOT_IMAGE);
        }
    }
}
