package com.depth.learningcrew.domain.qna.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.depth.learningcrew.domain.qna.dto.CommentDto;
import com.depth.learningcrew.domain.qna.service.CommentService;
import com.depth.learningcrew.system.security.model.UserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/study-groups/{studyGroupId}/qna/{qnaId}/comments")
@RequiredArgsConstructor
@Tag(name = "Q&A")
public class CommentController {

  private final CommentService commentService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "댓글(답변) 생성", description = "스터디 그룹 멤버가 질문에 댓글(답변)을 작성합니다.")
  @ApiResponse(responseCode = "201", description = "댓글 생성 성공")
  public CommentDto.CommentResponse createComment(
      @PathVariable Long studyGroupId,
      @PathVariable Long qnaId,
      @Valid @ModelAttribute CommentDto.CommentCreateRequest request,
      @Parameter(hidden = true) @org.springframework.security.core.annotation.AuthenticationPrincipal UserDetails userDetails) {

    return commentService.createComment(studyGroupId, qnaId, request, userDetails);
  }
}
