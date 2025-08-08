package com.depth.learningcrew.domain.studygroup.controller;

import com.depth.learningcrew.domain.studygroup.dto.GroupCategoryDto;
import com.depth.learningcrew.domain.studygroup.service.GroupCategoryService;
import com.depth.learningcrew.system.security.annotation.NoJwtAuth;
import com.depth.learningcrew.system.security.model.UserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-groups/categories")
@Tag(name = "Group Category", description = "카테고리 CRUD API")
public class GroupCategoryController {

    private final GroupCategoryService groupCategoryService;

    @NoJwtAuth
    @GetMapping
    @Operation(summary = "카테고리 목록 조회", description = "카테고리 목록 전체를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공")
    public List<GroupCategoryDto.GroupCategoryResponse> getGroupCategories() {
        return groupCategoryService.getGroupCategories();
    }

    @PatchMapping("/{categoryId}")
    @Operation(summary = "특정 카테고리 수정", description = "admin만 특정 카테고리를 수정할 수 있습니다. 모든 정보를 하나의 요청으로 전송합니다.")
    public GroupCategoryDto.GroupCategoryUpdateResponse updateGroupCategory(
            @PathVariable Integer categoryId,
            @Valid @RequestBody GroupCategoryDto.GroupCategoryUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return groupCategoryService.updateGroupCategory(categoryId, request, userDetails);
    }

    @PostMapping
    @Operation(summary = "카테고리 생성", description = "admin만 카테고리를 생성할 수 있습니다. 새로운 카테고리를 생성합니다.")
    @ApiResponse(responseCode = "201", description = "카테고리 생성 성공")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupCategoryDto.GroupCategoryResponse createGroupCategory(
            @Valid @RequestBody GroupCategoryDto.GroupCategoryCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return groupCategoryService.createGroupCategory(request, userDetails);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroupCategory(
            @PathVariable Integer categoryId,
            @AuthenticationPrincipal UserDetails userDetails) {
        groupCategoryService.deleteGroupCategory(categoryId, userDetails);
    }
}
