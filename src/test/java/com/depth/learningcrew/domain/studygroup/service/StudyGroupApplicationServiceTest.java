package com.depth.learningcrew.domain.studygroup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.depth.learningcrew.domain.studygroup.dto.ApplicationDto;
import com.depth.learningcrew.domain.studygroup.entity.Application;
import com.depth.learningcrew.domain.studygroup.entity.ApplicationId;
import com.depth.learningcrew.domain.studygroup.entity.Member;
import com.depth.learningcrew.domain.studygroup.entity.State;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.ApplicationRepository;
import com.depth.learningcrew.domain.studygroup.repository.MemberRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.domain.user.entity.Gender;
import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

@ExtendWith(MockitoExtension.class)
class StudyGroupApplicationServiceTest {

  @Mock
  private StudyGroupRepository studyGroupRepository;

  @Mock
  private ApplicationRepository applicationRepository;

  @Mock
  private MemberRepository memberRepository;

  @InjectMocks
  private StudyGroupApplicationService studyGroupApplicationService;

  private User owner;
  private User applicant;
  private StudyGroup studyGroup;
  private Application application;
  private UserDetails ownerDetails;
  private UserDetails applicantDetails;

  @BeforeEach
  void setUp() {
    owner = User.builder()
        .id(1)
        .email("owner@test.com")
        .password("password")
        .nickname("owner")
        .birthday(LocalDate.of(1990, 1, 1))
        .gender(Gender.MALE)
        .role(Role.USER)
        .build();

    applicant = User.builder()
        .id(2)
        .email("applicant@test.com")
        .password("password")
        .nickname("applicant")
        .birthday(LocalDate.of(1995, 5, 15))
        .gender(Gender.FEMALE)
        .role(Role.USER)
        .build();

    studyGroup = StudyGroup.builder()
        .id(1)
        .name("Test Study Group")
        .summary("Test Summary")
        .content("Test Content")
        .maxMembers(10)
        .memberCount(1)
        .currentStep(1)
        .startDate(LocalDate.now())
        .endDate(LocalDate.now().plusMonths(3))
        .owner(owner)
        .build();

    ApplicationId applicationId = ApplicationId.of(applicant, studyGroup);
    application = Application.builder()
        .id(applicationId)
        .state(State.PENDING)
        .build();

    ownerDetails = new UserDetails(owner);
    applicantDetails = new UserDetails(applicant);
  }

  @Test
  @DisplayName("가입 신청 수락 성공")
  void approveApplication_Success() {
    // given
    when(studyGroupRepository.findById(1)).thenReturn(Optional.of(studyGroup));
    when(applicationRepository.findById_User_IdAndId_StudyGroup_Id(2, 1)).thenReturn(Optional.of(application));
    when(memberRepository.existsById_UserAndId_StudyGroup(applicant, studyGroup)).thenReturn(false);
    when(memberRepository.save(any(Member.class))).thenReturn(new Member());
    when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(studyGroup);

    // when
    ApplicationDto.ApplicationResponse response = studyGroupApplicationService.approveApplication(1, 2, ownerDetails);

    // then
    assertThat(response.getState()).isEqualTo(State.APPROVED);
    assertThat(response.getApprovedAt()).isNotNull();
  }

  @Test
  @DisplayName("가입 신청 수락 실패 - 권한 없음")
  void approveApplication_Fail_NoPermission() {
    // given
    when(studyGroupRepository.findById(1)).thenReturn(Optional.of(studyGroup));
    when(applicationRepository.findById_User_IdAndId_StudyGroup_Id(2, 1)).thenReturn(Optional.of(application));

    // when & then
    assertThatThrownBy(() -> studyGroupApplicationService.approveApplication(1, 2, applicantDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);
  }

  @Test
  @DisplayName("가입 신청 수락 실패 - 이미 수락된 신청")
  void approveApplication_Fail_AlreadyApproved() {
    // given
    application.setState(State.APPROVED);
    when(studyGroupRepository.findById(1)).thenReturn(Optional.of(studyGroup));
    when(applicationRepository.findById_User_IdAndId_StudyGroup_Id(2, 1)).thenReturn(Optional.of(application));

    // when & then
    assertThatThrownBy(() -> studyGroupApplicationService.approveApplication(1, 2, ownerDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_GROUP_APPLICATION_ALREADY_APPROVED);
  }

  @Test
  @DisplayName("가입 신청 수락 실패 - 스터디 그룹 없음")
  void approveApplication_Fail_StudyGroupNotFound() {
    // given
    when(studyGroupRepository.findById(1)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> studyGroupApplicationService.approveApplication(1, 2, ownerDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GLOBAL_NOT_FOUND);
  }

  @Test
  @DisplayName("가입 신청 수락 실패 - 신청서 없음")
  void approveApplication_Fail_ApplicationNotFound() {
    // given
    when(studyGroupRepository.findById(1)).thenReturn(Optional.of(studyGroup));
    when(applicationRepository.findById_User_IdAndId_StudyGroup_Id(2, 1)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> studyGroupApplicationService.approveApplication(1, 2, ownerDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GLOBAL_NOT_FOUND);
  }

  @Test
  @DisplayName("가입 신청 수락 성공 - 이미 멤버인 경우")
  void approveApplication_Success_AlreadyMember() {
    // given
    when(studyGroupRepository.findById(1)).thenReturn(Optional.of(studyGroup));
    when(applicationRepository.findById_User_IdAndId_StudyGroup_Id(2, 1)).thenReturn(Optional.of(application));
    when(memberRepository.existsById_UserAndId_StudyGroup(applicant, studyGroup)).thenReturn(true);

    // when
    ApplicationDto.ApplicationResponse response = studyGroupApplicationService.approveApplication(1, 2, ownerDetails);

    // then
    assertThat(response.getState()).isEqualTo(State.APPROVED);
    assertThat(response.getApprovedAt()).isNotNull();
  }
}