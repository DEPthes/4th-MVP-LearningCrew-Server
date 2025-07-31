package com.depth.learningcrew.domain.studygroup.controller;

import com.depth.learningcrew.domain.studygroup.dto.GroupCategoryDto;
import com.depth.learningcrew.domain.studygroup.service.GroupCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-groups/categories")
@Tag(name = "Group Category", description = "카테고리 CRUD API")
public class GroupCategoryController {

    private final GroupCategoryService groupCategoryService;

    @GetMapping
    @Operation(summary = "카테고리 목록 조회", description = "카테고리 목록 전체를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공")
    public List<GroupCategoryDto.GroupCategoryResponse> getGroupCategories() {
        return groupCategoryService.getGroupCategories();
    }
}
