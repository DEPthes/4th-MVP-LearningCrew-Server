package com.depth.learningcrew.domain.file.dto;

import com.depth.learningcrew.domain.file.entity.AttachedFile;
import com.depth.learningcrew.domain.file.entity.HandlingType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

public class FileDto {

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class FileResponse {
        @Schema(description = "파일 id(UUID)", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
        private String uuid;
        @Schema(description = "원본 파일 이름", example = "image.jpg")
        private String fileName;
        @Schema(description = "파일 크기 (바이트 단위)", example = "204800")
        private Long size;
        @Schema(description = "파일 타입", example = "IMAGE | DOWNLOADABLE")
        private HandlingType handlingType;

        public static FileResponse from(AttachedFile file) {
            if(Objects.isNull(file)) {
                return null;
            }

            return FileResponse.builder()
                    .uuid(file.getUuid())
                    .fileName(file.getFileName())
                    .size(file.getSize())
                    .handlingType(file.getHandlingType())
                    .build();
        }
    }
}
