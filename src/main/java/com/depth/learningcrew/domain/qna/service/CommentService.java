package com.depth.learningcrew.domain.qna.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.depth.learningcrew.domain.file.entity.CommentAttachedFile;
import com.depth.learningcrew.domain.file.entity.CommentImageFile;
import com.depth.learningcrew.domain.file.handler.FileHandler;
import com.depth.learningcrew.domain.qna.dto.CommentDto;
import com.depth.learningcrew.domain.qna.entity.Comment;
import com.depth.learningcrew.domain.qna.entity.QAndA;
import com.depth.learningcrew.domain.qna.repository.CommentRepository;
import com.depth.learningcrew.domain.qna.repository.QAndARepository;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.MemberQueryRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final QAndARepository qAndARepository;
  private final StudyGroupRepository studyGroupRepository;
  private final MemberQueryRepository memberQueryRepository;
  private final FileHandler fileHandler;

  @Transactional
  public CommentDto.CommentResponse createComment(Long studyGroupId, Long qnaId,
      CommentDto.CommentCreateRequest request, UserDetails userDetails) {

    StudyGroup studyGroup = studyGroupRepository.findById(studyGroupId)
        .orElseThrow(() -> new RestException(ErrorCode.STUDY_GROUP_NOT_FOUND));
    QAndA qAndA = qAndARepository.findById(qnaId)
        .orElseThrow(() -> new RestException(ErrorCode.QANDA_NOT_FOUND));

    cannotCommentIfNotMember(studyGroup, userDetails);

    Comment toSave = request.toEntity();
    toSave.setQAndA(qAndA);

    Comment saved = commentRepository.save(toSave);

    saveAttachedImages(request.getAttachedImages(), saved);
    saveAttachedFiles(request.getAttachedFiles(), saved);

    return CommentDto.CommentResponse.from(saved);
  }

  @Transactional
  public CommentDto.CommentResponse updateComment(Long studyGroupId, Long commentId,
      CommentDto.CommentUpdateRequest request, UserDetails userDetails) {

    StudyGroup studyGroup = studyGroupRepository.findById(studyGroupId)
        .orElseThrow(() -> new RestException(ErrorCode.STUDY_GROUP_NOT_FOUND));

    cannotCommentIfNotMember(studyGroup, userDetails);

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new RestException(ErrorCode.COMMENT_NOT_FOUND));

    comment.canUpdateBy(userDetails);

    if (request.getContent() != null) {
      comment.setContent(request.getContent());
    }

    deleteAttachedImages(request.getDeletedAttachedImages(), comment);
    deleteAttachedFiles(request.getDeletedAttachedFiles(), comment);

    saveAttachedImages(request.getNewAttachedImages(), comment);
    saveAttachedFiles(request.getNewAttachedFiles(), comment);

    return CommentDto.CommentResponse.from(comment);
  }

  @Transactional
  public void deleteComment(Long commentId, UserDetails userDetails) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new RestException(ErrorCode.COMMENT_NOT_FOUND));

    // 권한 검증 (답변 작성자 또는 스터디 그룹 주최자)
    comment.canDeleteBy(userDetails);

    // 첨부 파일들 삭제
    comment.getAttachedImages().forEach(fileHandler::deleteFile);
    comment.getAttachedFiles().forEach(fileHandler::deleteFile);

    // 댓글 삭제
    commentRepository.delete(comment);
  }

  private void cannotCommentIfNotMember(StudyGroup studyGroup, UserDetails userDetails) {
    if (!memberQueryRepository.isMember(studyGroup, userDetails.getUser())) {
      throw new RestException(ErrorCode.AUTH_FORBIDDEN);
    }
  }

  private void saveAttachedImages(List<MultipartFile> files, Comment comment) {
    if (files == null || files.isEmpty())
      return;

    files.forEach(file -> {
      CommentImageFile image = CommentImageFile.from(file);
      image.setComment(comment);
      comment.addAttachedImage(image);
      fileHandler.saveFile(file, image);
    });
  }

  private void saveAttachedFiles(List<MultipartFile> files, Comment comment) {
    if (files == null || files.isEmpty())
      return;

    files.forEach(file -> {
      CommentAttachedFile attached = CommentAttachedFile.from(file);
      attached.setComment(comment);
      comment.addAttachedFile(attached);
      fileHandler.saveFile(file, attached);
    });
  }

  private void deleteAttachedImages(List<String> imageIds, Comment comment) {
    if (imageIds == null || imageIds.isEmpty())
      return;

    imageIds.forEach(id -> {
      CommentImageFile image = comment.getAttachedImages().stream()
          .filter(img -> id.equals(img.getUuid()))
          .findFirst()
          .orElse(null);
      if (image != null) {
        comment.removeAttachedImage(image);
        fileHandler.deleteFile(image);
      }
    });
  }

  private void deleteAttachedFiles(List<String> fileIds, Comment comment) {
    if (fileIds == null || fileIds.isEmpty())
      return;

    fileIds.forEach(id -> {
      CommentAttachedFile file = comment.getAttachedFiles().stream()
          .filter(f -> id.equals(f.getUuid()))
          .findFirst()
          .orElse(null);
      if (file != null) {
        comment.removeAttachedFile(file);
        fileHandler.deleteFile(file);
      }
    });
  }
}
