package com.depth.learningcrew.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "페이지네이션 응답 형식")
public class PaginationResponse<T> {

    @Schema(description = "응답 데이터 목록")
    private final List<T> content;

    @Schema(description = "페이지 정보")
    private final PageInfo page;

    @Getter
    @Builder
    @Schema(description = "페이지 정보")
    public static class PageInfo {
        @Schema(description = "페이지당 항목 수", example = "10")
        private final int size;

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        private final int pageNumber;

        @Schema(description = "전체 항목 수", example = "228")
        private final long totalElements;

        @Schema(description = "전체 페이지 수", example = "23")
        private final int totalPages;

        // RP
        // @Schema(description = "다음 페이지 존재 여부")
        // private final boolean hasNext;
        //
        // @Schema(description = "이전 페이지 존재 여부")
        // private final boolean hasPrevious;
    }

    public static <T> PaginationResponse<T> from(Page<T> page) {
        return PaginationResponse.<T>builder()
                .content(page.getContent())
                .page(PageInfo.builder()
                        .size(page.getSize())
                        .pageNumber(page.getNumber())
                        .totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build())
                .build();
    }
}
