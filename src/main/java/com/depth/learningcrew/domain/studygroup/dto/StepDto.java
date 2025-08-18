package com.depth.learningcrew.domain.studygroup.dto;

import java.time.LocalDate;
import java.util.List;

import com.depth.learningcrew.domain.file.dto.FileDto;
import com.depth.learningcrew.domain.studygroup.entity.StudyStep;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

public class StepDto {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Schema(description = "스터디 스텝 응답")
    public static class StepResponse {
        @Schema(description = "스텝 번호", example = "1")
        private Integer step;

        @Schema(description = "스텝 종료 날짜", example = "2025-08-15")
        private LocalDate endDate;

        @Schema(description = "스텝 제목", example = "1주차: 소개와 목표 설정")
        private String title;

        @Schema(description = "스텝 내용", example = "이 주차에는 스터디 방향을 정하고 환경을 세팅합니다.")
        private String content;

        @Schema(description = "첨부된 이미지 목록")
        private List<FileDto.FileResponse> attachedImages;

        @Schema(description = "첨부된 파일 목록")
        private List<FileDto.FileResponse> attachedFiles;

        public static StepResponse from(StudyStep step) {
            return StepResponse.builder()
                    .step(step.getId().getStep())
                    .endDate(step.getEndDate())
                    .title(step.getTitle())
                    .content(step.getContent())
                    .attachedImages(step.getImages().stream()
                            .map(FileDto.FileResponse::from)
                            .toList())
                    .attachedFiles(step.getFiles().stream()
                            .map(FileDto.FileResponse::from)
                            .toList())
                    .build();
        }
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Schema(description = "스텝 수정 요청")
    public static class StepUpdateRequest {
        @Schema(description = "스텝 제목", example = "1주차: 소개와 목표 설정")
        private String title;

        @Schema(description = "스텝 내용", example = "이 주차에는 스터디 방향을 정하고 환경을 세팅합니다.")
        private String content;

        @Schema(description = "새로 첨부된 이미지 목록")
        private List<MultipartFile> newAttachedImages;

        @Schema(description = "새로 첨부된 파일 목록")
        private List<MultipartFile> attachedFiles;

        @Schema(description = "삭제된 첨부 이미지 목록 (파일 UUID 목록)")
        private List<String> deletedAttachedImages;

        @Schema(description = "삭제된 첨부 파일 목록 (파일 UUID 목록)")
        private List<String> deletedAttachedFiles;
    }
}