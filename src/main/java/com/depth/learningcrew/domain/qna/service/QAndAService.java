package com.depth.learningcrew.domain.qna.service;

import java.util.List;
import java.util.Objects;

import com.depth.learningcrew.domain.qna.repository.QAndAQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.depth.learningcrew.domain.file.entity.QAndAAttachedFile;
import com.depth.learningcrew.domain.file.entity.QAndAImageFile;
import com.depth.learningcrew.domain.file.handler.FileHandler;
import com.depth.learningcrew.domain.qna.dto.QAndADto;
import com.depth.learningcrew.domain.qna.entity.QAndA;
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
public class QAndAService {

  private final QAndARepository qAndARepository;
  private final StudyGroupRepository studyGroupRepository;
  private final MemberQueryRepository memberQueryRepository;
  private final FileHandler fileHandler;
  private final QAndAQueryRepository qAndAQueryRepository;

  @Transactional
  public QAndADto.QAndADetailResponse createQAndA(
      QAndADto.QAndACreateRequest request,
      Long studyGroupId,
      Integer step,
      UserDetails user) {

    StudyGroup studyGroup = studyGroupRepository.findById(studyGroupId)
        .orElseThrow(() -> new RestException(ErrorCode.STUDY_GROUP_NOT_FOUND));

    cannotCreateWhenNotCurrentStep(studyGroup, step);
    cannotCreateWhenNotMember(studyGroup, user);

    QAndA toSave = request.toEntity();
    toSave.setStep(step);
    toSave.setStudyGroup(studyGroup);

    QAndA saved = qAndARepository.save(toSave);
    saveAttachedFiles(request.getAttachedFiles(), saved);
    saveAttachedImages(request.getAttachedImages(), saved);

    return QAndADto.QAndADetailResponse.from(saved);
  }

  @Transactional
  public QAndADto.QAndADetailResponse updateQAndA(
      Long qnaId,
      QAndADto.QAndAUpdateRequest request,
      UserDetails user) {

    QAndA qAndA = qAndARepository.findById(qnaId)
        .orElseThrow(() -> new RestException(ErrorCode.QANDA_NOT_FOUND));

    qAndA.canUpdateBy(user.getUser());

    if (request.getTitle() != null) {
      qAndA.setTitle(request.getTitle());
    }
    if (request.getContent() != null) {
      qAndA.setContent(request.getContent());
    }

    deleteAttachedFiles(request.getDeletedAttachedFiles(), qAndA);
    deleteAttachedImages(request.getDeletedAttachedImages(), qAndA);

    saveNewAttachedFiles(request.getNewAttachedFiles(), qAndA);
    saveNewAttachedImages(request.getNewAttachedImages(), qAndA);

    return QAndADto.QAndADetailResponse.from(qAndA);
  }

  @Transactional
  public void deleteQAndA(Long qnaId, UserDetails user) {
    QAndA qAndA = qAndARepository.findById(qnaId)
        .orElseThrow(() -> new RestException(ErrorCode.QANDA_NOT_FOUND));

    qAndA.canDeleteBy(user.getUser());

    deleteAllRelatedFiles(qAndA);
    qAndARepository.delete(qAndA);
  }

  @Transactional(readOnly = true)
  public PagedModel<QAndADto.QAndAResponse> paginateQAndAListByGroup(
          Long groupId,
          QAndADto.SearchConditions searchConditions,
          UserDetails user,
          Pageable pageable
  ) {
    StudyGroup studyGroup = studyGroupRepository.findById(groupId)
            .orElseThrow(() -> new RestException(ErrorCode.STUDY_GROUP_NOT_FOUND));

    if (!memberQueryRepository.isMember(studyGroup, user.getUser())) {
      throw new RestException(ErrorCode.AUTH_FORBIDDEN);
    }

    Page<QAndADto.QAndAResponse> page = qAndAQueryRepository
            .paginateByGroup(groupId, searchConditions, pageable);

    return new PagedModel<>(page);
  }

  private void cannotCreateWhenNotCurrentStep(StudyGroup studyGroup, Integer step) {
    if (!Objects.equals(studyGroup.getCurrentStep(), step)) {
      throw new RestException(ErrorCode.STUDY_GROUP_NOT_CURRENT_STEP);
    }
  }

  private void cannotCreateWhenNotMember(StudyGroup studyGroup, UserDetails user) {
    if (!memberQueryRepository.isMember(studyGroup, user.getUser())) {
      throw new RestException(ErrorCode.STUDY_GROUP_NOT_MEMBER);
    }
  }

  private void deleteAttachedFiles(List<String> fileIds, QAndA qAndA) {
    if (fileIds != null && !fileIds.isEmpty()) {
      fileIds.forEach(fileId -> {
        QAndAAttachedFile file = qAndA.getAttachedFiles().stream()
            .filter(f -> f.getUuid().equals(fileId))
            .findFirst()
            .orElse(null);
        if (file != null) {
          qAndA.removeAttachedFile(file);
          fileHandler.deleteFile(file);
        }
      });
    }
  }

  private void deleteAttachedImages(List<String> imageIds, QAndA qAndA) {
    if (imageIds != null && !imageIds.isEmpty()) {
      imageIds.forEach(imageId -> {
        QAndAImageFile image = qAndA.getAttachedImages().stream()
            .filter(img -> img.getUuid().equals(imageId))
            .findFirst()
            .orElse(null);
        if (image != null) {
          qAndA.removeAttachedImage(image);
          fileHandler.deleteFile(image);
        }
      });
    }
  }

  private void saveNewAttachedFiles(List<MultipartFile> files, QAndA qAndA) {
    if (files != null && !files.isEmpty()) {
      files.forEach(file -> {
        QAndAAttachedFile attachedFile = QAndAAttachedFile.from(file);
        attachedFile.setQAndA(qAndA);
        qAndA.addAttachedFile(attachedFile);
        fileHandler.saveFile(file, attachedFile);
      });
    }
  }

  private void saveNewAttachedImages(List<MultipartFile> files, QAndA qAndA) {
    if (files != null && !files.isEmpty()) {
      files.forEach(file -> {
        QAndAImageFile attachedImage = QAndAImageFile.from(file);
        attachedImage.setQAndA(qAndA);
        qAndA.addAttachedImage(attachedImage);
        fileHandler.saveFile(file, attachedImage);
      });
    }
  }

  private void saveAttachedFiles(List<MultipartFile> files, QAndA qAndA) {
    if (files != null && !files.isEmpty()) {
      files.forEach(file -> {
        QAndAAttachedFile attachedFile = QAndAAttachedFile.from(file);
        attachedFile.setQAndA(qAndA);
        qAndA.addAttachedFile(attachedFile);
        fileHandler.saveFile(file, attachedFile);
      });
    }
  }

  private void saveAttachedImages(List<MultipartFile> files, QAndA qAndA) {
    if (files != null && !files.isEmpty()) {
      files.forEach(file -> {
        QAndAImageFile attachedImage = QAndAImageFile.from(file);
        attachedImage.setQAndA(qAndA);
        qAndA.addAttachedImage(attachedImage);
        fileHandler.saveFile(file, attachedImage);
      });
    }
  }

  private void deleteAllRelatedFiles(QAndA qAndA) {
    // 질문 첨부 파일 삭제
    if (qAndA.getAttachedFiles() != null) {
      qAndA.getAttachedFiles().forEach(fileHandler::deleteFile);
    }
    if (qAndA.getAttachedImages() != null) {
      qAndA.getAttachedImages().forEach(fileHandler::deleteFile);
    }

    // 댓글 첨부 이미지 삭제
    if (qAndA.getComments() != null) {
      qAndA.getComments().forEach(comment -> {
        if (comment.getAttachedImages() != null) {
          comment.getAttachedImages().forEach(fileHandler::deleteFile);
        }
      });
    }
  }
}
