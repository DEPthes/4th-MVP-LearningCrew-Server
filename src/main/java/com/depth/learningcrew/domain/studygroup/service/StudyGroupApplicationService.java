package com.depth.learningcrew.domain.studygroup.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.depth.learningcrew.domain.studygroup.dto.ApplicationDto;
import com.depth.learningcrew.domain.studygroup.entity.Application;
import com.depth.learningcrew.domain.studygroup.entity.ApplicationId;
import com.depth.learningcrew.domain.studygroup.entity.State;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.ApplicationRepository;
import com.depth.learningcrew.domain.studygroup.repository.MemberRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudyGroupApplicationService {

  private final StudyGroupRepository studyGroupRepository;
  private final ApplicationRepository applicationRepository;
  private final MemberRepository memberRepository;

  @Transactional
  public ApplicationDto.ApplicationResponse joinStudyGroup(Integer groupId, UserDetails userDetails) {
    StudyGroup studyGroup = studyGroupRepository.findById(groupId)
        .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

    cannotApplicateIfAlreadyMember(userDetails, studyGroup);
    cannotApplicateIfAlreadySubmit(userDetails, studyGroup);

    ApplicationId applicationId = ApplicationId.of(userDetails.getUser(), studyGroup);
    Application application = Application.builder()
        .id(applicationId)
        .state(State.PENDING)
        .build();

    Application savedApplication = applicationRepository.save(application);
    return ApplicationDto.ApplicationResponse.from(savedApplication);
  }

  private void cannotApplicateIfAlreadyMember(UserDetails userDetails, StudyGroup studyGroup) {
    if (memberRepository.existsById_UserAndId_StudyGroup(userDetails.getUser(), studyGroup)) {
      throw new RestException(ErrorCode.STUDY_GROUP_ALREADY_MEMBER);
    }
  }

  private void cannotApplicateIfAlreadySubmit(UserDetails userDetails, StudyGroup studyGroup) {
    if (applicationRepository.existsById_UserAndId_StudyGroup(userDetails.getUser(), studyGroup)) {
      throw new RestException(ErrorCode.STUDY_GROUP_ALREADY_APPLIED);
    }
  }
}