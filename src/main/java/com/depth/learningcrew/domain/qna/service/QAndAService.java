package com.depth.learningcrew.domain.qna.service;

import java.util.List;
import java.util.Objects;

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

  @Transactional
  public QAndADto.QAndAResponse createQAndA(
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

    return QAndADto.QAndAResponse.from(saved);
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

}
