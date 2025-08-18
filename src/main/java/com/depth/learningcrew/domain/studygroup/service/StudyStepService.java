package com.depth.learningcrew.domain.studygroup.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.depth.learningcrew.domain.file.entity.StudyStepFile;
import com.depth.learningcrew.domain.file.entity.StudyStepImage;
import com.depth.learningcrew.domain.file.handler.FileHandler;
import com.depth.learningcrew.domain.studygroup.dto.StepDto;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.entity.StudyStep;
import com.depth.learningcrew.domain.studygroup.entity.StudyStepId;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyStepRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudyStepService {

  private final StudyStepRepository studyStepRepository;
  private final StudyGroupRepository studyGroupRepository;
  private final FileHandler fileHandler;

  @Transactional(readOnly = true)
  public StepDto.StepResponse getStep(Long groupId, Integer stepNumber) {
    StudyGroup group = studyGroupRepository.findById(groupId)
        .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

    StudyStepId id = StudyStepId.of(stepNumber, group);
    StudyStep step = studyStepRepository.findById(id)
        .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

    return StepDto.StepResponse.from(step);
  }

  @Transactional
  public StepDto.StepResponse updateStep(Long groupId, Integer stepNumber, StepDto.StepUpdateRequest request,
      UserDetails user) {
    StudyGroup group = studyGroupRepository.findById(groupId)
        .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

    group.canUpdateBy(user);

    StudyStepId id = StudyStepId.of(stepNumber, group);
    StudyStep step = studyStepRepository.findByIdForUpdate(id)
        .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

    if (request.getTitle() != null) {
      step.setTitle(request.getTitle());
    }
    if (request.getContent() != null) {
      step.setContent(request.getContent());
    }

    deleteAttachedFiles(request.getDeletedAttachedFiles(), step);
    deleteAttachedImages(request.getDeletedAttachedImages(), step);
    saveAttachedFiles(request.getNewAttachedFiles(), step);
    saveAttachedImages(request.getNewAttachedImages(), step);

    return StepDto.StepResponse.from(step);
  }

  private void saveAttachedFiles(List<MultipartFile> files, StudyStep step) {
    if (files != null && !files.isEmpty()) {
      files.forEach(file -> {
        if (file == null || file.isEmpty()) {
          return;
        }
        StudyStepFile attachedFile = StudyStepFile.from(file);
        if (attachedFile == null) {
          return;
        }
        attachedFile.setStudyStep(step);
        step.getFiles().add(attachedFile);
        fileHandler.saveFile(file, attachedFile);
      });
    }
  }

  private void saveAttachedImages(List<MultipartFile> images, StudyStep step) {
    if (images != null && !images.isEmpty()) {
      images.forEach(image -> {
        if (image == null || image.isEmpty()) {
          return;
        }
        StudyStepImage imageFile = StudyStepImage.from(image);
        if (imageFile == null) {
          return;
        }
        imageFile.setStudyStep(step);
        step.getImages().add(imageFile);
        fileHandler.saveFile(image, imageFile);
      });
    }
  }

  public void deleteAttachedFiles(List<String> fileIds, StudyStep step) {
    if (fileIds != null && !fileIds.isEmpty()) {
      fileIds.forEach(fileId -> {
        StudyStepFile attachedFile = step.getFiles().stream()
            .filter(f -> f.getUuid().equals(fileId))
            .findFirst()
            .orElse(null);
        if (attachedFile != null) {
          step.getFiles().remove(attachedFile);
          fileHandler.deleteFile(attachedFile);
        }
      });
    }
  }

  public void deleteAttachedImages(List<String> imageIds, StudyStep step) {
    if (imageIds != null && !imageIds.isEmpty()) {
      imageIds.forEach(imageId -> {
        StudyStepImage imageFile = step.getImages().stream()
            .filter(f -> f.getUuid().equals(imageId))
            .findFirst()
            .orElse(null);
        if (imageFile != null) {
          step.getImages().remove(imageFile);
          fileHandler.deleteFile(imageFile);
        }
      });
    }
  }
}
