package com.depth.learningcrew.domain.studygroup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

import com.depth.learningcrew.domain.studygroup.dto.ApplicationDto;
import com.depth.learningcrew.domain.studygroup.entity.Application;
import com.depth.learningcrew.domain.studygroup.entity.ApplicationId;
import com.depth.learningcrew.domain.studygroup.entity.Member;
import com.depth.learningcrew.domain.studygroup.entity.State;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.ApplicationQueryRepository;
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

  @Mock
  private ApplicationQueryRepository applicationQueryRepository;

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
        .id(1L)
        .email("owner@test.com")
        .password("password")
        .nickname("owner")
        .birthday(LocalDate.of(1990, 1, 1))
        .gender(Gender.MALE)
        .role(Role.USER)
        .build();

    applicant = User.builder()
        .id(2L)
        .email("applicant@test.com")
        .password("password")
        .nickname("applicant")
        .birthday(LocalDate.of(1995, 5, 15))
        .gender(Gender.FEMALE)
        .role(Role.USER)
        .build();

    studyGroup = StudyGroup.builder()
        .id(1L)
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
    when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(studyGroup));
    when(applicationRepository.findById_User_IdAndId_StudyGroup_Id(2L, 1L)).thenReturn(Optional.of(application));
    when(memberRepository.existsById_UserAndId_StudyGroup(applicant, studyGroup)).thenReturn(false);
    when(memberRepository.save(any(Member.class))).thenReturn(new Member());
    when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(studyGroup);

    // when
    ApplicationDto.ApplicationResponse response = studyGroupApplicationService.approveApplication(1L, 2L, ownerDetails);

    // then
    assertThat(response.getState()).isEqualTo(State.APPROVED);
    assertThat(response.getApprovedAt()).isNotNull();
  }

  @Test
  @DisplayName("가입 신청 수락 실패 - 권한 없음")
  void approveApplication_Fail_NoPermission() {
    // given
    when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(studyGroup));
    when(applicationRepository.findById_User_IdAndId_StudyGroup_Id(2L, 1L)).thenReturn(Optional.of(application));

    // when & then
    assertThatThrownBy(() -> studyGroupApplicationService.approveApplication(1L, 2L, applicantDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);
  }

  @Test
  @DisplayName("가입 신청 수락 실패 - 이미 수락된 신청")
  void approveApplication_Fail_AlreadyApproved() {
    // given
    application.setState(State.APPROVED);
    when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(studyGroup));
    when(applicationRepository.findById_User_IdAndId_StudyGroup_Id(2L, 1L)).thenReturn(Optional.of(application));

    // when & then
    assertThatThrownBy(() -> studyGroupApplicationService.approveApplication(1L, 2L, ownerDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_GROUP_APPLICATION_ALREADY_APPROVED);
  }

  @Test
  @DisplayName("가입 신청 수락 실패 - 스터디 그룹 없음")
  void approveApplication_Fail_StudyGroupNotFound() {
    // given
    when(studyGroupRepository.findById(1L)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> studyGroupApplicationService.approveApplication(1L, 2L, ownerDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GLOBAL_NOT_FOUND);
  }

  @Test
  @DisplayName("가입 신청 수락 실패 - 신청서 없음")
  void approveApplication_Fail_ApplicationNotFound() {
    // given
    when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(studyGroup));
    when(applicationRepository.findById_User_IdAndId_StudyGroup_Id(2L, 1L)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> studyGroupApplicationService.approveApplication(1L, 2L, ownerDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GLOBAL_NOT_FOUND);
  }

  @Test
  @DisplayName("가입 신청 성공 - 이미 멤버인 경우")
  void approveApplication_Success_AlreadyMember() {
    // given
    when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(studyGroup));
    when(applicationRepository.findById_User_IdAndId_StudyGroup_Id(2L, 1L)).thenReturn(Optional.of(application));
    when(memberRepository.existsById_UserAndId_StudyGroup(applicant, studyGroup)).thenReturn(true);

    // when
    ApplicationDto.ApplicationResponse response = studyGroupApplicationService.approveApplication(1L, 2L, ownerDetails);

    // then
    assertThat(response.getState()).isEqualTo(State.APPROVED);
    assertThat(response.getApprovedAt()).isNotNull();
  }

  @Test
  @DisplayName("가입 신청 거절 성공")
  void rejectApplication_Success() {
    // given
    when(studyGroupRepository.existsById(1L)).thenReturn(true);
    when(applicationRepository.findById_User_IdAndId_StudyGroup_Id(2L, 1L)).thenReturn(Optional.of(application));

    // when
    ApplicationDto.ApplicationResponse response = studyGroupApplicationService.rejectApplication(1L, 2L, ownerDetails);

    // then
    assertThat(response.getState()).isEqualTo(State.REJECTED);
    assertThat(response.getApprovedAt()).isNotNull();
  }

  @Test
  @DisplayName("가입 신청 거절 실패 - 권한 없음")
  void rejectApplication_Fail_NoPermission() {
    // given
    when(studyGroupRepository.existsById(1L)).thenReturn(true);
    when(applicationRepository.findById_User_IdAndId_StudyGroup_Id(2L, 1L)).thenReturn(Optional.of(application));

    // when & then
    assertThatThrownBy(() -> studyGroupApplicationService.rejectApplication(1L, 2L, applicantDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);
  }

  @Test
  @DisplayName("가입 신청 거절 실패 - 이미 거절된 신청")
  void rejectApplication_Fail_AlreadyRejected() {
    // given
    application.setState(State.REJECTED);
    when(studyGroupRepository.existsById(1L)).thenReturn(true);
    when(applicationRepository.findById_User_IdAndId_StudyGroup_Id(2L, 1L)).thenReturn(Optional.of(application));

    // when & then
    assertThatThrownBy(() -> studyGroupApplicationService.rejectApplication(1L, 2L, ownerDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_GROUP_APPLICATION_ALREADY_REJECTED);
  }

  @Test
  @DisplayName("가입 신청 거절 실패 - 이미 승인된 신청")
  void rejectApplication_Fail_AlreadyApproved() {
    // given
    application.setState(State.APPROVED);
    when(studyGroupRepository.existsById(1L)).thenReturn(true);
    when(applicationRepository.findById_User_IdAndId_StudyGroup_Id(2L, 1L)).thenReturn(Optional.of(application));

    // when & then
    assertThatThrownBy(() -> studyGroupApplicationService.rejectApplication(1L, 2L, ownerDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_GROUP_APPLICATION_ALREADY_APPROVED);
  }

  @Test
  @DisplayName("가입 신청 거절 실패 - 스터디 그룹 없음")
  void rejectApplication_Fail_StudyGroupNotFound() {
    // given
    when(studyGroupRepository.existsById(1L)).thenReturn(false);

    // when & then
    assertThatThrownBy(() -> studyGroupApplicationService.rejectApplication(1L, 2L, ownerDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GLOBAL_NOT_FOUND);
  }

  @Test
  @DisplayName("가입 신청 거절 실패 - 신청서 없음")
  void rejectApplication_Fail_ApplicationNotFound() {
    // given
    when(studyGroupRepository.existsById(1L)).thenReturn(true);
    when(applicationRepository.findById_User_IdAndId_StudyGroup_Id(2L, 1L)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> studyGroupApplicationService.rejectApplication(1L, 2L, ownerDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GLOBAL_NOT_FOUND);
  }

  @Test
  @DisplayName("내 가입 신청 목록 조회 성공 - 기본 조회")
  void getMyApplications_Success_DefaultSearch() {
    // given
    Pageable pageable = PageRequest.of(0, 10);
    ApplicationDto.SearchConditions searchConditions = ApplicationDto.SearchConditions.builder().build();

    List<ApplicationDto.ApplicationResponse> applicationResponses = List.of(
        ApplicationDto.ApplicationResponse.builder()
            .user(com.depth.learningcrew.domain.user.dto.UserDto.UserResponse.builder()
                .id(applicant.getId())
                .email(applicant.getEmail())
                .nickname(applicant.getNickname())
                .build())
            .studyGroup(com.depth.learningcrew.domain.studygroup.dto.StudyGroupDto.StudyGroupResponse.builder()
                .id(studyGroup.getId())
                .name(studyGroup.getName())
                .summary(studyGroup.getSummary())
                .build())
            .state(State.PENDING)
            .createdAt(LocalDate.now().atStartOfDay())
            .build());

    Page<ApplicationDto.ApplicationResponse> page = new PageImpl<>(applicationResponses, pageable, 1);

    when(applicationQueryRepository.paginateApplicationsByUserId(applicant, searchConditions, pageable))
        .thenReturn(page);

    // when
    PagedModel<ApplicationDto.ApplicationResponse> result = studyGroupApplicationService
        .getMyApplications(searchConditions, pageable, applicantDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getState()).isEqualTo(State.PENDING);
    assertThat(result.getContent().get(0).getUser().getId()).isEqualTo(applicant.getId());
    assertThat(result.getContent().get(0).getStudyGroup().getId()).isEqualTo(studyGroup.getId());
  }

  @Test
  @DisplayName("내 가입 신청 목록 조회 성공 - 상태 필터링")
  void getMyApplications_Success_StateFilter() {
    // given
    Pageable pageable = PageRequest.of(0, 10);
    ApplicationDto.SearchConditions searchConditions = ApplicationDto.SearchConditions.builder()
        .state(State.APPROVED)
        .build();

    List<ApplicationDto.ApplicationResponse> applicationResponses = List.of(
        ApplicationDto.ApplicationResponse.builder()
            .user(com.depth.learningcrew.domain.user.dto.UserDto.UserResponse.builder()
                .id(applicant.getId())
                .email(applicant.getEmail())
                .nickname(applicant.getNickname())
                .build())
            .studyGroup(com.depth.learningcrew.domain.studygroup.dto.StudyGroupDto.StudyGroupResponse.builder()
                .id(studyGroup.getId())
                .name(studyGroup.getName())
                .summary(studyGroup.getSummary())
                .build())
            .state(State.APPROVED)
            .createdAt(LocalDate.now().atStartOfDay())
            .build());

    Page<ApplicationDto.ApplicationResponse> page = new PageImpl<>(applicationResponses, pageable, 1);

    when(applicationQueryRepository.paginateApplicationsByUserId(applicant, searchConditions, pageable))
        .thenReturn(page);

    // when
    PagedModel<ApplicationDto.ApplicationResponse> result = studyGroupApplicationService
        .getMyApplications(searchConditions, pageable, applicantDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getState()).isEqualTo(State.APPROVED);
  }

  @Test
  @DisplayName("내 가입 신청 목록 조회 성공 - 빈 결과")
  void getMyApplications_Success_EmptyResult() {
    // given
    Pageable pageable = PageRequest.of(0, 10);
    ApplicationDto.SearchConditions searchConditions = ApplicationDto.SearchConditions.builder().build();

    Page<ApplicationDto.ApplicationResponse> page = new PageImpl<>(List.of(), pageable, 0);

    when(applicationQueryRepository.paginateApplicationsByUserId(applicant, searchConditions, pageable))
        .thenReturn(page);

    // when
    PagedModel<ApplicationDto.ApplicationResponse> result = studyGroupApplicationService
        .getMyApplications(searchConditions, pageable, applicantDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();
  }

  @Test
  @DisplayName("내 가입 신청 목록 조회 성공 - 페이지네이션")
  void getMyApplications_Success_Pagination() {
    // given
    Pageable pageable = PageRequest.of(1, 5); // 두 번째 페이지, 5개씩
    ApplicationDto.SearchConditions searchConditions = ApplicationDto.SearchConditions.builder().build();

    List<ApplicationDto.ApplicationResponse> applicationResponses = List.of(
        ApplicationDto.ApplicationResponse.builder()
            .user(com.depth.learningcrew.domain.user.dto.UserDto.UserResponse.builder()
                .id(applicant.getId())
                .email(applicant.getEmail())
                .nickname(applicant.getNickname())
                .build())
            .studyGroup(com.depth.learningcrew.domain.studygroup.dto.StudyGroupDto.StudyGroupResponse.builder()
                .id(studyGroup.getId())
                .name(studyGroup.getName())
                .summary(studyGroup.getSummary())
                .build())
            .state(State.PENDING)
            .createdAt(LocalDate.now().atStartOfDay())
            .build());

    Page<ApplicationDto.ApplicationResponse> page = new PageImpl<>(applicationResponses, pageable, 10);

    when(applicationQueryRepository.paginateApplicationsByUserId(applicant, searchConditions, pageable))
        .thenReturn(page);

    // when
    PagedModel<ApplicationDto.ApplicationResponse> result = studyGroupApplicationService
        .getMyApplications(searchConditions, pageable, applicantDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  @DisplayName("내 가입 신청 목록 조회 성공 - 정렬 조건")
  void getMyApplications_Success_SortConditions() {
    // given
    Pageable pageable = PageRequest.of(0, 10);
    ApplicationDto.SearchConditions searchConditions = ApplicationDto.SearchConditions.builder()
        .sort("alphabet")
        .order("asc")
        .build();

    List<ApplicationDto.ApplicationResponse> applicationResponses = List.of(
        ApplicationDto.ApplicationResponse.builder()
            .user(com.depth.learningcrew.domain.user.dto.UserDto.UserResponse.builder()
                .id(applicant.getId())
                .email(applicant.getEmail())
                .nickname(applicant.getNickname())
                .build())
            .studyGroup(com.depth.learningcrew.domain.studygroup.dto.StudyGroupDto.StudyGroupResponse.builder()
                .id(studyGroup.getId())
                .name(studyGroup.getName())
                .summary(studyGroup.getSummary())
                .build())
            .state(State.PENDING)
            .createdAt(LocalDate.now().atStartOfDay())
            .build());

    Page<ApplicationDto.ApplicationResponse> page = new PageImpl<>(applicationResponses, pageable, 1);

    when(applicationQueryRepository.paginateApplicationsByUserId(applicant, searchConditions, pageable))
        .thenReturn(page);

    // when
    PagedModel<ApplicationDto.ApplicationResponse> result = studyGroupApplicationService
        .getMyApplications(searchConditions, pageable, applicantDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
  }
}