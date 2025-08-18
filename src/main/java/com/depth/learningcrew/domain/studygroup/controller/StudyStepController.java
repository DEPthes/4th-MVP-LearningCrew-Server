package com.depth.learningcrew.domain.studygroup.controller;

import com.depth.learningcrew.system.security.annotation.NoJwtAuth;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.depth.learningcrew.domain.studygroup.dto.StepDto;
import com.depth.learningcrew.domain.studygroup.service.StudyStepService;
import com.depth.learningcrew.system.security.model.UserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-groups/{groupId}/steps")
@Tag(name = "Study Step", description = "스터디 스텝 조회/수정 API")
public class StudyStepController {

  private final StudyStepService studyStepService;

  @GetMapping("/{step}")
  @Operation(summary = "특정 스텝 조회", description = "그룹 ID와 스텝 번호로 스텝 정보를 조회합니다.")
  @NoJwtAuth
  public StepDto.StepResponse getStep(
      @PathVariable Long groupId,
      @PathVariable Integer step) {
    return studyStepService.getStep(groupId, step);
  }

  @PatchMapping("/{step}")
  @Operation(summary = "특정 스텝 수정", description = "owner 또는 관리자만 스텝의 제목/내용을 수정할 수 있습니다.")
  public StepDto.StepResponse updateStep(
      @PathVariable Long groupId,
      @PathVariable Integer step,
      @Valid @ModelAttribute StepDto.StepUpdateRequest request,
      @AuthenticationPrincipal UserDetails user) {
    return studyStepService.updateStep(groupId, step, request, user);
  }
}
