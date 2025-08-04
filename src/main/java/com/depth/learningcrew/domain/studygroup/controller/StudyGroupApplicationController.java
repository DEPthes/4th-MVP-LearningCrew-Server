package com.depth.learningcrew.domain.studygroup.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @GetMapping("/{groupId}/applications")
    @Operation(summary = "스터디 그룹 가입 신청 목록 조회", description = "스터디 그룹의 owner가 가입 신청 목록을 페이지네이션으로 조회합니다.")
    public PagedModel<ApplicationDto.ApplicationResponse> getApplicationsByGroupId(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Integer groupId,
            @ModelAttribute @ParameterObject ApplicationDto.SearchConditions searchConditions,
            @PageableDefault @ParameterObject Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        return studyGroupApplicationService
                .getApplicationsByGroupId(groupId, searchConditions, userDetails, pageable);
    }

    @PostMapping("/{groupId}/join")
    @Operation(summary = "스터디 그룹 가입 신청", description = "로그인한 사용자가 특정 스터디 그룹에 가입 신청을 합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationDto.ApplicationResponse joinStudyGroup(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Integer groupId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        return studyGroupApplicationService.joinStudyGroup(groupId, userDetails);
    }

    @PostMapping("/{groupId}/applications/{userId}/approve")
    @Operation(summary = "스터디 그룹 가입 신청 수락", description = "스터디 그룹의 owner가 가입 신청을 수락합니다.")
    public ApplicationDto.ApplicationResponse approveApplication(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Integer groupId,
            @Parameter(description = "신청자 ID") @PathVariable Integer userId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        return studyGroupApplicationService.approveApplication(groupId, userId, userDetails);
    }

    @PostMapping("/{groupId}/applications/{userId}/reject")
    @Operation(summary = "스터디 그룹 가입 신청 거절", description = "스터디 그룹의 owner가 가입 신청을 거절합니다.")
    public ApplicationDto.ApplicationResponse rejectApplication(
            @Parameter(description = "스터디 그룹 ID") @PathVariable Integer groupId,
            @Parameter(description = "신청자 ID") @PathVariable Integer userId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        return studyGroupApplicationService.rejectApplication(groupId, userId, userDetails);
    }

    @GetMapping("/my/applications")
    @Operation(summary = "내 가입 신청 목록 조회", description = "로그인한 사용자가 자신의 가입 신청 목록을 페이지네이션으로 조회합니다.")
    public PagedModel<ApplicationDto.ApplicationResponse> getMyApplications(
            @ModelAttribute @ParameterObject ApplicationDto.SearchConditions searchConditions,
            @PageableDefault @ParameterObject Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        return studyGroupApplicationService.getMyApplications(searchConditions, pageable, userDetails);
    }
}