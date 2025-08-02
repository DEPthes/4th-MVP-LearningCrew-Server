package com.depth.learningcrew.domain.studygroup.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.depth.learningcrew.domain.studygroup.dto.ApplicationDto;
import com.depth.learningcrew.domain.studygroup.service.StudyGroupApplicationService;
import com.depth.learningcrew.system.security.model.UserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-groups")
@Tag(name = "Study Group Application", description = "스터디 그룹 가입 신청 API")
public class StudyGroupApplicationController {

  private final StudyGroupApplicationService studyGroupApplicationService;

  @PostMapping("/{groupId}/join")
  @Operation(summary = "스터디 그룹 가입 신청", description = "로그인한 사용자가 특정 스터디 그룹에 가입 신청을 합니다.")
  public ResponseEntity<ApplicationDto.ApplicationResponse> joinStudyGroup(
      @Parameter(description = "스터디 그룹 ID") @PathVariable Integer groupId,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    ApplicationDto.ApplicationResponse response = studyGroupApplicationService.joinStudyGroup(groupId, userDetails);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/{groupId}/applications/{userId}/approve")
  @Operation(summary = "스터디 그룹 가입 신청 수락", description = "스터디 그룹의 owner가 가입 신청을 수락합니다.")
  public ResponseEntity<ApplicationDto.ApplicationResponse> approveApplication(
      @Parameter(description = "스터디 그룹 ID") @PathVariable Integer groupId,
      @Parameter(description = "신청자 ID") @PathVariable Integer userId,
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
    ApplicationDto.ApplicationResponse response = studyGroupApplicationService.approveApplication(groupId, userId,
        userDetails);
    return ResponseEntity.ok(response);
  }
}