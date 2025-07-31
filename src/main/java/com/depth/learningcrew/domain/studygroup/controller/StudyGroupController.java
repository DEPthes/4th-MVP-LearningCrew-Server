package com.depth.learningcrew.domain.studygroup.controller;

import com.depth.learningcrew.domain.studygroup.dto.StudyGroupDto;
import com.depth.learningcrew.domain.studygroup.service.StudyGroupService;
import com.depth.learningcrew.system.security.model.UserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-groups")
@Tag(name = "Study Group", description = "스터디 그룹 CRUD API")
public class StudyGroupController {

  private final StudyGroupService studyGroupService;

  @GetMapping("/my/owned")
  @Operation(summary = "내 주최 그룹 목록 조회", description = "로그인한 사용자가 주최한 스터디 그룹 목록을 조건에 맞게 페이지네이션하여 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "내 주최 그룹 목록 조회 성공", content = @Content(schema = @Schema(implementation = PagedModel.class))),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  public PagedModel<StudyGroupDto.StudyGroupPaginationResponse> getMyOwnedStudyGroups(
      @ModelAttribute StudyGroupDto.SearchConditions searchConditions,
      @PageableDefault Pageable pageable,
      @AuthenticationPrincipal UserDetails userDetails) {
    return studyGroupService.paginateMyOwnedStudyGroups(searchConditions, userDetails, pageable);
  }
}