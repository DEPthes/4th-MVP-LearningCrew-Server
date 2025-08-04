package com.depth.learningcrew.domain.studygroup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

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
import com.depth.learningcrew.domain.studygroup.entity.Member;
import com.depth.learningcrew.domain.studygroup.entity.MemberId;
import com.depth.learningcrew.domain.studygroup.entity.State;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.ApplicationRepository;
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
        private ApplicationRepository applicationRepository;

        @Autowired
        private MemberRepository memberRepository;

        @Autowired
        private UserRepository userRepository;

        private User owner;
        private User applicant;
        private StudyGroup studyGroup;
        private Application application;
        private UserDetails ownerDetails;
        private UserDetails applicantDetails;

        @BeforeEach
        void setUp() {
                // 사용자 생성
                owner = User.builder()
                                .email("owner@test.com")
                                .password("password")
                                .nickname("owner")
                                .birthday(LocalDate.of(1990, 1, 1))
                                .gender(Gender.MALE)
                                .role(Role.USER)
                                .build();
                owner = userRepository.save(owner);

                applicant = User.builder()
                                .email("applicant@test.com")
                                .password("password")
                                .nickname("applicant")
                                .birthday(LocalDate.of(1995, 5, 15))
                                .gender(Gender.FEMALE)
                                .role(Role.USER)
                                .build();
                applicant = userRepository.save(applicant);

                // 스터디 그룹 생성
                studyGroup = StudyGroup.builder()
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
                studyGroup = studyGroupRepository.save(studyGroup);

                // 가입 신청 생성
                ApplicationId applicationId = ApplicationId.of(applicant, studyGroup);
                application = Application.builder()
                                .id(applicationId)
                                .state(State.PENDING)
                                .build();
                application = applicationRepository.save(application);

                ownerDetails = new UserDetails(owner);
                applicantDetails = new UserDetails(applicant);
        }

        @Test
        @DisplayName("가입 신청 수락 통합 테스트 - 성공")
        void approveApplication_Integration_Success() {
                // when
                ApplicationDto.ApplicationResponse response = studyGroupApplicationService.approveApplication(
                                studyGroup.getId(), applicant.getId(), ownerDetails);

                // then
                assertThat(response.getState()).isEqualTo(State.APPROVED);
                assertThat(response.getApprovedAt()).isNotNull();
                assertThat(response.getUser().getId()).isEqualTo(applicant.getId());
                assertThat(response.getStudyGroup().getId()).isEqualTo(studyGroup.getId());

                // 데이터베이스에서 확인
                Application savedApplication = applicationRepository.findById(application.getId()).orElse(null);
                assertThat(savedApplication).isNotNull();
                assertThat(savedApplication.getState()).isEqualTo(State.APPROVED);
                assertThat(savedApplication.getApprovedAt()).isNotNull();

                // 멤버로 추가되었는지 확인
                Member member = memberRepository.findById_UserAndId_StudyGroup(applicant, studyGroup).orElse(null);
                assertThat(member).isNotNull();

                // 스터디 그룹 멤버 수 증가 확인
                StudyGroup updatedStudyGroup = studyGroupRepository.findById(studyGroup.getId()).orElse(null);
                assertThat(updatedStudyGroup).isNotNull();
                assertThat(updatedStudyGroup.getMemberCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("가입 신청 수락 통합 테스트 - 권한 없음")
        void approveApplication_Integration_NoPermission() {
                // when & then
                assertThatThrownBy(() -> studyGroupApplicationService.approveApplication(
                                studyGroup.getId(), applicant.getId(), applicantDetails))
                                .isInstanceOf(RestException.class)
                                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);
        }

        @Test
        @DisplayName("가입 신청 수락 통합 테스트 - 이미 수락된 신청")
        void approveApplication_Integration_AlreadyApproved() {
                // given
                application.approve();
                applicationRepository.save(application);

                // when & then
                assertThatThrownBy(() -> studyGroupApplicationService.approveApplication(
                                studyGroup.getId(), applicant.getId(), ownerDetails))
                                .isInstanceOf(RestException.class)
                                .hasFieldOrPropertyWithValue("errorCode",
                                                ErrorCode.STUDY_GROUP_APPLICATION_ALREADY_APPROVED);
        }

        @Test
        @DisplayName("가입 신청 수락 통합 테스트 - 이미 멤버인 경우")
        void approveApplication_Integration_AlreadyMember() {
                // given - 이미 멤버로 등록
                MemberId memberId = MemberId.of(applicant, studyGroup);
                Member member = Member.builder()
                                .id(memberId)
                                .build();
                memberRepository.save(member);

                // when
                ApplicationDto.ApplicationResponse response = studyGroupApplicationService.approveApplication(
                                studyGroup.getId(), applicant.getId(), ownerDetails);

                // then
                assertThat(response.getState()).isEqualTo(State.APPROVED);
                assertThat(response.getApprovedAt()).isNotNull();

                // 데이터베이스에서 확인
                Application savedApplication = applicationRepository.findById(application.getId()).orElse(null);
                assertThat(savedApplication).isNotNull();
                assertThat(savedApplication.getState()).isEqualTo(State.APPROVED);
                assertThat(savedApplication.getApprovedAt()).isNotNull();

                // 스터디 그룹 멤버 수는 증가하지 않아야 함 (이미 멤버였으므로)
                StudyGroup updatedStudyGroup = studyGroupRepository.findById(studyGroup.getId()).orElse(null);
                assertThat(updatedStudyGroup).isNotNull();
                assertThat(updatedStudyGroup.getMemberCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("가입 신청 거절 통합 테스트 - 성공")
        void rejectApplication_Integration_Success() {
                // when
                ApplicationDto.ApplicationResponse response = studyGroupApplicationService.rejectApplication(
                                studyGroup.getId(), applicant.getId(), ownerDetails);

                // then
                assertThat(response.getState()).isEqualTo(State.REJECTED);
                assertThat(response.getApprovedAt()).isNotNull();
                assertThat(response.getUser().getId()).isEqualTo(applicant.getId());
                assertThat(response.getStudyGroup().getId()).isEqualTo(studyGroup.getId());

                // 데이터베이스에서 확인
                Application savedApplication = applicationRepository.findById(application.getId()).orElse(null);
                assertThat(savedApplication).isNotNull();
                assertThat(savedApplication.getState()).isEqualTo(State.REJECTED);
                assertThat(savedApplication.getApprovedAt()).isNotNull();

                // 멤버로 추가되지 않았는지 확인
                Member member = memberRepository.findById_UserAndId_StudyGroup(applicant, studyGroup).orElse(null);
                assertThat(member).isNull();

                // 스터디 그룹 멤버 수는 변경되지 않아야 함
                StudyGroup updatedStudyGroup = studyGroupRepository.findById(studyGroup.getId()).orElse(null);
                assertThat(updatedStudyGroup).isNotNull();
                assertThat(updatedStudyGroup.getMemberCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("가입 신청 거절 통합 테스트 - 권한 없음")
        void rejectApplication_Integration_NoPermission() {
                // when & then
                assertThatThrownBy(() -> studyGroupApplicationService.rejectApplication(
                                studyGroup.getId(), applicant.getId(), applicantDetails))
                                .isInstanceOf(RestException.class)
                                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);
        }

        @Test
        @DisplayName("가입 신청 거절 통합 테스트 - 이미 거절된 신청")
        void rejectApplication_Integration_AlreadyRejected() {
                // given
                application.reject();
                applicationRepository.save(application);

                // when & then
                assertThatThrownBy(() -> studyGroupApplicationService.rejectApplication(
                                studyGroup.getId(), applicant.getId(), ownerDetails))
                                .isInstanceOf(RestException.class)
                                .hasFieldOrPropertyWithValue("errorCode",
                                                ErrorCode.STUDY_GROUP_APPLICATION_ALREADY_REJECTED);
        }

        @Test
        @DisplayName("가입 신청 거절 통합 테스트 - 이미 승인된 신청")
        void rejectApplication_Integration_AlreadyApproved() {
                // given
                application.approve();
                applicationRepository.save(application);

                // when & then
                assertThatThrownBy(() -> studyGroupApplicationService.rejectApplication(
                                studyGroup.getId(), applicant.getId(), ownerDetails))
                                .isInstanceOf(RestException.class)
                                .hasFieldOrPropertyWithValue("errorCode",
                                                ErrorCode.STUDY_GROUP_APPLICATION_ALREADY_APPROVED);
        }
}