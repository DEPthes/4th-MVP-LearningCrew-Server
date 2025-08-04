package com.depth.learningcrew.domain.studygroup.controller;

import com.depth.learningcrew.common.response.PaginationResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import com.depth.learningcrew.domain.studygroup.dto.StudyGroupDto;
import com.depth.learningcrew.domain.studygroup.service.StudyGroupService;
import com.depth.learningcrew.system.security.model.UserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-groups")
@Tag(name = "Study Group", description = "스터디 그룹 CRUD API")
public class StudyGroupController {

  private final StudyGroupService studyGroupService;

  @GetMapping("/my/owned")
  @Operation(summary = "내 주최 그룹 목록 조회", description = "로그인한 사용자가 주최한 스터디 그룹 목록을 조건에 맞게 페이지네이션하여 조회합니다.")
  public PaginationResponse<StudyGroupDto.StudyGroupResponse> getMyOwnedStudyGroups(
      @ModelAttribute @ParameterObject StudyGroupDto.SearchConditions searchConditions,
      @PageableDefault @ParameterObject Pageable pageable,
      @AuthenticationPrincipal UserDetails userDetails) {
    return studyGroupService.paginateMyOwnedStudyGroups(searchConditions, userDetails, pageable);
  }

  @PatchMapping(value = "/{groupId}")
  @Operation(summary = "스터디 그룹 정보 수정", description = "owner만 스터디 그룹의 정보를 수정할 수 있습니다. 모든 정보를 하나의 요청으로 전송합니다.")
  public StudyGroupDto.StudyGroupResponse updateStudyGroup(
      @PathVariable Integer groupId,
      @Valid @ModelAttribute @ParameterObject StudyGroupDto.StudyGroupUpdateRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {
    return studyGroupService.updateStudyGroup(groupId, request, userDetails);
  }

    @GetMapping("/{id}")
    @Operation(summary = "스터디 그룹 상세 조회", description = "스터디 그룹 ID를 통해 스터디 그룹의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "스터디 그룹 조회 성공")
    public StudyGroupDto.StudyGroupDetailResponse getStudyGroup(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails){
        return studyGroupService.getStudyGroupDetail(id, userDetails);
      }

  @GetMapping
  @Operation(summary = "전체 스터디 그룹 목록 조회", description = "조건에 맞는 모든 스터디 그룹을 페이지네이션하여 조회합니다.")
  public PaginationResponse<StudyGroupDto.StudyGroupResponse> getAllStudyGroups(
          @ModelAttribute @ParameterObject StudyGroupDto.SearchConditions searchConditions,
          @PageableDefault(page = 0, size = 10) @ParameterObject Pageable pageable,
          @AuthenticationPrincipal UserDetails userDetails // nullable
  ) {
    return studyGroupService.paginateAllStudyGroups(searchConditions, userDetails, pageable);
  }
}