package com.depth.learningcrew.domain.note.dto;

import com.depth.learningcrew.domain.file.dto.FileDto;
import com.depth.learningcrew.domain.note.entity.Note;
import com.depth.learningcrew.domain.user.dto.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public class NoteDto {

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Schema(description = "노트 생성 요청 DTO")
    public static class NoteCreateRequest {
        @Schema(description = "노트 제목")
        private String title;

        @Schema(description = "노트 내용")
        private String content;

        @Schema(description = "첨부 파일 목록")
        private List<MultipartFile> attachedFiles;

        @Schema(description = "첨부 이미지 목록")
        private List<MultipartFile> attachedImages;

        public Note toEntity() {
            return Note.builder()
                    .title(title)
                    .content(content)
                    .build();
        }
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Schema(description = "노트 응답 DTO")
    public static class NoteResponse {
        @Schema(description = "노트 ID", example = "123")
        private Long id;

        @Schema(description = "해당 노트 스텝", example = "1")
        private Integer step;

        @Schema(description = "노트 제목", example = "note title")
        private String title;

        @Schema(description = "노트 내용", example = "note content")
        private String content;

        @Schema(description = "첨부 파일 목록")
        private List<FileDto.FileResponse> attachedFiles;

        @Schema(description = "첨부 이미지 목록")
        private List<FileDto.FileResponse> attachedImages;

        @Schema(description = "노트 작성자 정보")
        private UserDto.UserResponse createdBy;

        @Schema(description = "노트 마지막 수정자 정보")
        private UserDto.UserResponse updatedBy;

        @Schema(description = "노트 생성 시간")
        private LocalDateTime createdAt;

        @Schema(description = "노트 마지막 수정 시간")
        private LocalDateTime lastModifiedAt;

        public static NoteResponse from(Note note) {
            return NoteResponse.builder()
                    .id(note.getId())
                    .step(note.getStep())
                    .title(note.getTitle())
                    .content(note.getContent())
                    .attachedFiles(note.getAttachedFiles().stream().map(FileDto.FileResponse::from).toList())
                    .attachedImages(note.getAttachedImages().stream().map(FileDto.FileResponse::from).toList())
                    .createdBy(UserDto.UserResponse.from(note.getCreatedBy()))
                    .updatedBy(UserDto.UserResponse.from(note.getLastModifiedBy()))
                    .createdAt(note.getCreatedAt())
                    .lastModifiedAt(note.getLastModifiedAt())
                    .build();
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Getter
    @Setter
    @Schema(description = "노트 수정 요청 DTO")
    public static class NoteUpdateRequest {
        @Schema(description = "노트 제목", example = "note title")
        private String title;

        @Schema(description = "노트 내용", example = "note content")
        private String content;

        @Schema(description = "새로 추가할 첨부 파일 목록")
        private List<MultipartFile> newAttachedFiles;

        @Schema(description = "새로 추가할 첨부 이미지 목록")
        private List<MultipartFile> newAttachedImages;

        @Schema(description = "삭제할 첨부 파일 ID 목록")
        private List<String> deletedAttachedFiles;

        @Schema(description = "삭제할 이미지 파일 ID 목록")
        private List<String> deletedAttachedImages;
    }

}
