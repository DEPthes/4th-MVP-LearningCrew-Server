package com.depth.learningcrew.domain.qna.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.depth.learningcrew.domain.qna.dto.QAndADto;
import com.depth.learningcrew.domain.qna.service.QAndAService;
import com.depth.learningcrew.system.security.model.UserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Q&A", description = "질문과 답변 API")
public class QAndAController {

  private final QAndAService qAndAService;

  @PostMapping(value = "/study-groups/{groupId}/steps/{stepId}/questions")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "질문 생성", description = "특정 스터디 그룹의 스텝에 질문을 생성합니다.")
  public QAndADto.QAndADetailResponse createQAndA(
      @Parameter(description = "스터디 그룹 ID", example = "1") @PathVariable Long groupId,
      @Parameter(description = "스터디 스텝 번호", example = "1") @PathVariable Integer stepId,
      @Parameter(description = "질문 생성 요청") @Valid @ModelAttribute QAndADto.QAndACreateRequest request,
      @AuthenticationPrincipal UserDetails user) {

    return qAndAService.createQAndA(request, groupId, stepId, user);
  }

  @PatchMapping(value = "/qna/{qnaId}")
  @Operation(summary = "질문 수정", description = "스터디 그룹의 질문을 수정합니다. 질문 작성자 또는 스터디 그룹 주최자만 수정할 수 있습니다.")
  public QAndADto.QAndADetailResponse updateQAndA(
      @Parameter(description = "질문 ID", example = "42") @PathVariable Long qnaId,
      @Parameter(description = "질문 수정 요청") @Valid @ModelAttribute QAndADto.QAndAUpdateRequest request,
      @AuthenticationPrincipal UserDetails user) {

    return qAndAService.updateQAndA(qnaId, request, user);
  }

  @DeleteMapping(value = "/qna/{qnaId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "질문 삭제", description = "스터디 그룹의 질문을 삭제합니다. 질문 작성자 또는 스터디 그룹 주최자만 삭제할 수 있습니다.")
  public void deleteQAndA(
      @Parameter(description = "질문 ID", example = "42") @PathVariable Long qnaId,
      @AuthenticationPrincipal UserDetails user) {

    qAndAService.deleteQAndA(qnaId, user);
  }

  @GetMapping("/study-groups/{studyGroupId}/qna")
  @Operation(summary = "스터디 그룹 질문 목록 조회", description = "그룹 멤버만 해당 그룹의 질문을 조건에 맞게 페이지네이션하여 조회")
  public PagedModel<QAndADto.QAndAResponse> getGroupQAndAs(
          @PathVariable Long studyGroupId,
          @ModelAttribute @ParameterObject QAndADto.SearchConditions searchConditions,
          @PageableDefault(page = 0, size = 10) @ParameterObject Pageable pageable,
          @AuthenticationPrincipal UserDetails userDetails
  ) {
    return qAndAService.paginateQAndAListByGroup(studyGroupId, searchConditions, userDetails, pageable);
  }

  @GetMapping("/study-groups/{studyGroupId}/qna/{qnaId}")
  @Operation(summary = "스터디 그룹 질문 상세 조회", description = "그룹 멤버만 해당 그룹의 질문 상세를 조회할 수 있다.")
  public QAndADto.QAndADetailResponse getQAndADetail(
          @PathVariable Long studyGroupId,
          @PathVariable Long qnaId,
          @AuthenticationPrincipal UserDetails userDetails
  ){
    return qAndAService.getQAndADetail(studyGroupId, qnaId, userDetails);
  }
}
