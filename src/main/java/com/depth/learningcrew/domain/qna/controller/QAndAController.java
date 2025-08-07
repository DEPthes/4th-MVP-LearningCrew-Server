package com.depth.learningcrew.domain.qna.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.depth.learningcrew.domain.qna.dto.QAndADto;
import com.depth.learningcrew.domain.qna.service.QAndAService;
import com.depth.learningcrew.system.security.model.UserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/study-groups")
@RequiredArgsConstructor
@Tag(name = "Q&A", description = "질문과 답변 API")
public class QAndAController {

  private final QAndAService qAndAService;

  @PostMapping(value = "/{groupId}/steps/{stepId}/questions")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "질문 생성", description = "특정 스터디 그룹의 스텝에 질문을 생성합니다.")
  public QAndADto.QAndAResponse createQAndA(
      @Parameter(description = "스터디 그룹 ID", example = "1") @PathVariable Long groupId,
      @Parameter(description = "스터디 스텝 번호", example = "1") @PathVariable Integer stepId,
      @Parameter(description = "질문 생성 요청") @Valid @ModelAttribute QAndADto.QAndACreateRequest request,
      @Parameter(hidden = true) UserDetails user) {

    return qAndAService.createQAndA(request, groupId, stepId, user);
  }
}
