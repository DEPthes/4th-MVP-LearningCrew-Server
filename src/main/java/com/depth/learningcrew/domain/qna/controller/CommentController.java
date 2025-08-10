package com.depth.learningcrew.domain.qna.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.depth.learningcrew.domain.qna.dto.CommentDto;
import com.depth.learningcrew.domain.qna.service.CommentService;
import com.depth.learningcrew.system.security.model.UserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Q&A")
public class CommentController {

  private final CommentService commentService;

  @PostMapping("/study-groups/{studyGroupId}/qna/{qnaId}/comments")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "댓글(답변) 생성", description = "스터디 그룹 멤버가 질문에 댓글(답변)을 작성합니다.")
  @ApiResponse(responseCode = "201", description = "댓글 생성 성공")
  public CommentDto.CommentResponse createComment(
      @PathVariable Long studyGroupId,
      @PathVariable Long qnaId,
      @Valid @ModelAttribute CommentDto.CommentCreateRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {

    return commentService.createComment(studyGroupId, qnaId, request, userDetails);
  }

  @PatchMapping("/study-groups/{studyGroupId}/qna/{qnaId}/comments/{commentId}")
  @Operation(summary = "댓글(답변) 수정", description = "스터디 그룹 멤버가 작성한 댓글(답변)을 수정합니다. 작성자 또는 스터디 그룹 주최자/관리자만 수정할 수 있습니다.")
  @ApiResponse(responseCode = "200", description = "댓글 수정 성공")
  public CommentDto.CommentResponse updateComment(
      @PathVariable Long studyGroupId,
      @PathVariable Long commentId,
      @Valid @ModelAttribute CommentDto.CommentUpdateRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {

    return commentService.updateComment(studyGroupId, commentId, request, userDetails);
  }

  @DeleteMapping("/comments/{commentId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "댓글(답변) 삭제", description = "답변 작성자 또는 스터디 그룹 주최자가 작성된 답변을 삭제합니다.")
  @ApiResponse(responseCode = "204", description = "댓글 삭제 성공")
  public void deleteComment(
      @PathVariable Long commentId,
      @AuthenticationPrincipal UserDetails userDetails) {

    commentService.deleteComment(commentId, userDetails);
  }
}
