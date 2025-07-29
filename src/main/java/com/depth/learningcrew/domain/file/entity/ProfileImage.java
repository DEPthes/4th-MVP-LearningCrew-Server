package com.depth.learningcrew.domain.file.entity;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.depth.learningcrew.domain.user.entity.User;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@DiscriminatorValue("PROFILE_IMAGE")
public class ProfileImage extends AttachedFile {
    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    public static ProfileImage from(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        return ProfileImage.builder()
                .uuid(UUID.randomUUID().toString())
                .handingType(HandingType.IMAGE)
                .fileName(file.getOriginalFilename())
                .size(file.getSize())
                .build();
    }
}
