package com.depth.learningcrew.domain.studygroup.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.depth.learningcrew.domain.studygroup.dto.StudyGroupDto;
import com.depth.learningcrew.domain.studygroup.service.StudyGroupService;
import com.depth.learningcrew.system.security.model.UserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "내 주최 그룹 목록 조회 성공", content = @Content(schema = @Schema(implementation = PagedModel.class))),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  public PagedModel<StudyGroupDto.StudyGroupResponse> getMyOwnedStudyGroups(
      @ModelAttribute StudyGroupDto.SearchConditions searchConditions,
      @PageableDefault Pageable pageable,
      @AuthenticationPrincipal UserDetails userDetails) {
    return studyGroupService.paginateMyOwnedStudyGroups(searchConditions, userDetails, pageable);
  }

  @PatchMapping(value = "/{groupId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "스터디 그룹 정보 수정", description = "owner만 스터디 그룹의 정보를 수정할 수 있습니다. 텍스트 정보는 request 파트(JSON), 이미지는 groupImage 파트로 전송.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = StudyGroupDto.StudyGroupResponse.class))),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "404", description = "스터디 그룹 없음")
  })
  public StudyGroupDto.StudyGroupResponse updateStudyGroup(
      @PathVariable Integer groupId,
      @Valid @RequestPart(value = "request", required = false) StudyGroupDto.StudyGroupUpdateRequest request,
      @RequestPart(value = "groupImage", required = false) MultipartFile groupImage,
      @AuthenticationPrincipal UserDetails userDetails) {
    return studyGroupService.updateStudyGroup(groupId, request, groupImage, userDetails);
  }
}