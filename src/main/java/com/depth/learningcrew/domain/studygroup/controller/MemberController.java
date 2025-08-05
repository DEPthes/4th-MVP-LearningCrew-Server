package com.depth.learningcrew.domain.studygroup.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.depth.learningcrew.domain.studygroup.dto.MemberDto;
import com.depth.learningcrew.domain.studygroup.service.MemberService;
import com.depth.learningcrew.system.security.model.UserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/study-groups/{groupId}/members")
@Tag(name = "Study Group Member", description = "스터디 그룹 멤버 API")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;

  @GetMapping
  @Operation(summary = "스터디 그룹 멤버 조회", description = "그룹의 owner 권한을 가진 사용자가 스터디 그룹의 멤버를 페이징하여 조회합니다.")
  public PagedModel<MemberDto.MemberResponse> getStudyGroupMembers(@PathVariable Long groupId,
      @ModelAttribute @ParameterObject MemberDto.SearchConditions searchConditions,
      @PageableDefault(page = 0, size = 10) @ParameterObject Pageable pageable,
      @AuthenticationPrincipal UserDetails userDetails) {

    return memberService.paginateStudyGroupMembers(groupId, searchConditions, userDetails, pageable);
  }

}
