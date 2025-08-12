package com.depth.learningcrew.domain.studygroup.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    return StepDto.StepResponse.from(step);
  }
}
