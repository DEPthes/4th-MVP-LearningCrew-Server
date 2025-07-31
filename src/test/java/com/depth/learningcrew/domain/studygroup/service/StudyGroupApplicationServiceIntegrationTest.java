package com.depth.learningcrew.domain.studygroup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.depth.learningcrew.domain.studygroup.dto.ApplicationDto;
import com.depth.learningcrew.domain.studygroup.entity.Application;
import com.depth.learningcrew.domain.studygroup.entity.ApplicationId;
import com.depth.learningcrew.domain.studygroup.entity.GroupCategory;
import com.depth.learningcrew.domain.studygroup.entity.Member;
import com.depth.learningcrew.domain.studygroup.entity.MemberId;
import com.depth.learningcrew.domain.studygroup.entity.State;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.ApplicationRepository;
import com.depth.learningcrew.domain.studygroup.repository.GroupCategoryRepository;
import com.depth.learningcrew.domain.studygroup.repository.MemberRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.domain.user.entity.Gender;
import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.domain.user.repository.UserRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StudyGroupApplicationServiceIntegrationTest {

  @Autowired
  private StudyGroupApplicationService studyGroupApplicationService;

  @Autowired
  private StudyGroupRepository studyGroupRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ApplicationRepository applicationRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private GroupCategoryRepository groupCategoryRepository;

  private User testUser;
  private User ownerUser;
  private StudyGroup testStudyGroup;
  private GroupCategory testCategory;
  private UserDetails userDetails;

  @BeforeEach
  void setUp() {
    // 테스트 사용자 생성
    testUser = User.builder()
        .email("test@example.com")
        .password("password")
        .nickname("testUser")
        .birthday(LocalDate.of(1990, 1, 1))
        .gender(Gender.MALE)
        .role(Role.USER)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();
    testUser = userRepository.save(testUser);

    // 스터디 그룹 주최자 생성
    ownerUser = User.builder()
        .email("owner@example.com")
        .password("password")
        .nickname("ownerUser")
        .birthday(LocalDate.of(1990, 1, 1))
        .gender(Gender.MALE)
        .role(Role.USER)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();
    ownerUser = userRepository.save(ownerUser);

    // 테스트 카테고리 생성
    testCategory = GroupCategory.builder()
        .name("테스트 카테고리")
        .build();
    testCategory = groupCategoryRepository.save(testCategory);

    // 테스트 스터디 그룹 생성
    testStudyGroup = StudyGroup.builder()
        .name("테스트 스터디 그룹")
        .summary("테스트 스터디 그룹입니다.")
        .content("테스트 스터디 그룹 내용입니다.")
        .maxMembers(10)
        .memberCount(1)
        .currentStep(1)
        .startDate(LocalDate.now())
        .endDate(LocalDate.now().plusMonths(3))
        .owner(ownerUser)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();
    testStudyGroup = studyGroupRepository.save(testStudyGroup);

    // UserDetails 설정
    userDetails = UserDetails.builder()
        .user(testUser)
        .build();
  }

  @Test
  @DisplayName("정상적으로 가입 신청이 되는 경우")
  void joinStudyGroup_success() {
    // when
    ApplicationDto.ApplicationResponse response = studyGroupApplicationService.joinStudyGroup(
        testStudyGroup.getId(), userDetails);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getState()).isEqualTo(State.PENDING);
    assertThat(response.getUser().getEmail()).isEqualTo(testUser.getEmail());
    assertThat(response.getStudyGroup().getId()).isEqualTo(testStudyGroup.getId());

    // 데이터베이스에 실제로 저장되었는지 확인
    ApplicationId applicationId = ApplicationId.of(testUser, testStudyGroup);
    Application savedApplication = applicationRepository.findById(applicationId).orElse(null);
    assertThat(savedApplication).isNotNull();
    assertThat(savedApplication.getState()).isEqualTo(State.PENDING);
  }

  @Test
  @DisplayName("이미 멤버인 경우 예외 발생")
  void joinStudyGroup_alreadyMember() {
    // given - 멤버로 등록
    MemberId memberId = MemberId.builder()
        .user(testUser)
        .studyGroup(testStudyGroup)
        .build();
    Member member = Member.builder()
        .id(memberId)
        .build();
    memberRepository.save(member);

    // when & then
    assertThatThrownBy(() -> studyGroupApplicationService.joinStudyGroup(testStudyGroup.getId(), userDetails))
        .isInstanceOf(RestException.class)
        .hasMessageContaining(ErrorCode.STUDY_GROUP_ALREADY_MEMBER.getMessage());
  }

  @Test
  @DisplayName("이미 신청한 경우 예외 발생")
  void joinStudyGroup_alreadyApplied() {
    // given - 이미 신청한 상태
    ApplicationId applicationId = ApplicationId.of(testUser, testStudyGroup);
    Application application = Application.builder()
        .id(applicationId)
        .state(State.PENDING)
        .build();
    applicationRepository.save(application);

    // when & then
    assertThatThrownBy(() -> studyGroupApplicationService.joinStudyGroup(testStudyGroup.getId(), userDetails))
        .isInstanceOf(RestException.class)
        .hasMessageContaining(ErrorCode.STUDY_GROUP_ALREADY_APPLIED.getMessage());
  }

  @Test
  @DisplayName("스터디 그룹이 존재하지 않는 경우 예외 발생")
  void joinStudyGroup_groupNotFound() {
    // when & then
    assertThatThrownBy(() -> studyGroupApplicationService.joinStudyGroup(99999, userDetails))
        .isInstanceOf(RestException.class)
        .hasMessageContaining(ErrorCode.GLOBAL_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("중복 신청 방지 확인")
  void joinStudyGroup_duplicateApplicationPrevention() {
    // given - 첫 번째 신청
    ApplicationDto.ApplicationResponse firstResponse = studyGroupApplicationService.joinStudyGroup(
        testStudyGroup.getId(), userDetails);
    assertThat(firstResponse.getState()).isEqualTo(State.PENDING);

    // when & then - 두 번째 신청 시도 시 예외 발생
    assertThatThrownBy(() -> studyGroupApplicationService.joinStudyGroup(testStudyGroup.getId(), userDetails))
        .isInstanceOf(RestException.class)
        .hasMessageContaining(ErrorCode.STUDY_GROUP_ALREADY_APPLIED.getMessage());
  }
}