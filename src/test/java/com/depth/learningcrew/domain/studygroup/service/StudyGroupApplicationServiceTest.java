package com.depth.learningcrew.domain.studygroup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.depth.learningcrew.domain.studygroup.dto.ApplicationDto;
import com.depth.learningcrew.domain.studygroup.entity.Application;
import com.depth.learningcrew.domain.studygroup.entity.State;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.ApplicationRepository;
import com.depth.learningcrew.domain.studygroup.repository.MemberRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

class StudyGroupApplicationServiceTest {

  @Mock
  private StudyGroupRepository studyGroupRepository;
  @Mock
  private ApplicationRepository applicationRepository;
  @Mock
  private MemberRepository memberRepository;
  @Mock
  private UserDetails userDetails;
  @Mock
  private User user;

  @InjectMocks
  private StudyGroupApplicationService studyGroupApplicationService;

  private StudyGroup studyGroup;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    studyGroup = StudyGroup.builder().id(1).categories(new java.util.ArrayList<>()).owner(user).build();
    when(userDetails.getUser()).thenReturn(user);
  }

  @Test
  @DisplayName("정상적으로 가입 신청이 되는 경우")
  void joinStudyGroup_success() {
    // given
    when(studyGroupRepository.findById(1)).thenReturn(Optional.of(studyGroup));
    when(memberRepository.existsById_UserAndId_StudyGroup(user, studyGroup)).thenReturn(false);
    when(applicationRepository.existsById_UserAndId_StudyGroup(user, studyGroup)).thenReturn(false);
    when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    ApplicationDto.ApplicationResponse response = studyGroupApplicationService.joinStudyGroup(1, userDetails);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getState()).isEqualTo(State.PENDING);
  }

  @Test
  @DisplayName("이미 멤버인 경우 예외 발생")
  void joinStudyGroup_alreadyMember() {
    // given
    when(studyGroupRepository.findById(1)).thenReturn(Optional.of(studyGroup));
    when(memberRepository.existsById_UserAndId_StudyGroup(user, studyGroup)).thenReturn(true);

    // when & then
    assertThatThrownBy(() -> studyGroupApplicationService.joinStudyGroup(1, userDetails))
        .isInstanceOf(RestException.class)
        .hasMessageContaining(ErrorCode.STUDY_GROUP_ALREADY_MEMBER.getMessage());
  }

  @Test
  @DisplayName("이미 신청한 경우 예외 발생")
  void joinStudyGroup_alreadyApplied() {
    // given
    when(studyGroupRepository.findById(1)).thenReturn(Optional.of(studyGroup));
    when(memberRepository.existsById_UserAndId_StudyGroup(user, studyGroup)).thenReturn(false);
    when(applicationRepository.existsById_UserAndId_StudyGroup(user, studyGroup)).thenReturn(true);

    // when & then
    assertThatThrownBy(() -> studyGroupApplicationService.joinStudyGroup(1, userDetails))
        .isInstanceOf(RestException.class)
        .hasMessageContaining(ErrorCode.STUDY_GROUP_ALREADY_APPLIED.getMessage());
  }

  @Test
  @DisplayName("스터디 그룹이 존재하지 않는 경우 예외 발생")
  void joinStudyGroup_groupNotFound() {
    // given
    when(studyGroupRepository.findById(1)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> studyGroupApplicationService.joinStudyGroup(1, userDetails))
        .isInstanceOf(RestException.class)
        .hasMessageContaining(ErrorCode.GLOBAL_NOT_FOUND.getMessage());
  }
}