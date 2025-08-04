package com.depth.learningcrew.domain.studygroup.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.depth.learningcrew.domain.studygroup.dto.ApplicationDto;
import com.depth.learningcrew.domain.studygroup.entity.Application;
import com.depth.learningcrew.domain.studygroup.entity.ApplicationId;
import com.depth.learningcrew.domain.studygroup.entity.Member;
import com.depth.learningcrew.domain.studygroup.entity.MemberId;
import com.depth.learningcrew.domain.studygroup.entity.State;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.ApplicationQueryRepository;
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
  private final ApplicationQueryRepository applicationQueryRepository;
  private final MemberRepository memberRepository;

  @Transactional(readOnly = true)
  public PagedModel<ApplicationDto.ApplicationResponse> getApplicationsByGroupId(
      Long groupId,
      ApplicationDto.SearchConditions searchConditions,
      UserDetails userDetails,
      Pageable pageable) {

    // 스터디 그룹이 존재하고 요청자가 owner인지 확인
    StudyGroup studyGroup = studyGroupRepository.findById(groupId)
        .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

    if (!studyGroup.getOwner().getId().equals(userDetails.getUser().getId())) {
      throw new RestException(ErrorCode.AUTH_FORBIDDEN);
    }

    Page<ApplicationDto.ApplicationResponse> result = applicationQueryRepository.paginateApplicationsByGroupId(
        groupId, searchConditions, userDetails, pageable);

    return new PagedModel<>(result);
  }

  @Transactional
  public ApplicationDto.ApplicationResponse joinStudyGroup(Long groupId, UserDetails userDetails) {
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

  @Transactional
  public ApplicationDto.ApplicationResponse approveApplication(Long groupId, Long userId,
      UserDetails ownerDetails) {
    StudyGroup studyGroup = studyGroupRepository.findById(groupId)
        .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

    Application application = applicationRepository.findById_User_IdAndId_StudyGroup_Id(userId, groupId)
        .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

    application.canApprovedBy(ownerDetails);
    application.canApproveNow();
    application.approve();

    boolean isAlreadyMember = memberRepository.existsById_UserAndId_StudyGroup(application.getId().getUser(),
        studyGroup);

    if (isAlreadyMember) {
      return ApplicationDto.ApplicationResponse.from(application);
    }

    MemberId memberId = MemberId.of(application.getId().getUser(), studyGroup);
    Member member = new Member(memberId);
    memberRepository.save(member);

    // 스터디 그룹 멤버 수 증가
    studyGroup.setMemberCount(studyGroup.getMemberCount() + 1);
    studyGroupRepository.save(studyGroup);

    return ApplicationDto.ApplicationResponse.from(application);
  }

  @Transactional
  public ApplicationDto.ApplicationResponse rejectApplication(Long groupId, Long userId, UserDetails ownerDetails) {
    if (!studyGroupRepository.existsById(groupId)) {
      throw new RestException(ErrorCode.GLOBAL_NOT_FOUND);
    }

    Application application = applicationRepository.findById_User_IdAndId_StudyGroup_Id(userId, groupId)
        .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

    application.canRejectBy(ownerDetails);
    application.canRejectNow();
    application.reject();

    return ApplicationDto.ApplicationResponse.from(application);
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

  @Transactional(readOnly = true)
  public PagedModel<ApplicationDto.ApplicationResponse> getMyApplications(
      ApplicationDto.SearchConditions searchConditions,
      Pageable pageable,
      UserDetails userDetails) {
    Page<ApplicationDto.ApplicationResponse> result = applicationQueryRepository.paginateApplicationsByUserId(
        userDetails.getUser(), searchConditions, pageable);

    return new PagedModel<>(result);
  }
}