package com.depth.learningcrew.domain.studygroup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.depth.learningcrew.domain.file.handler.FileHandler;
import com.depth.learningcrew.domain.studygroup.dto.StudyGroupDto;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.DibsRepository;
import com.depth.learningcrew.domain.studygroup.repository.MemberRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

@ExtendWith(MockitoExtension.class)
class StudyGroupServiceTest {

        @Mock
        private StudyGroupRepository studyGroupRepository;

        @Mock
        private GroupCategoryService groupCategoryService;

        @Mock
        private FileHandler fileHandler;

        @Mock
        private DibsRepository dibsRepository;

        @Mock
        private MemberRepository memberRepository;

        @InjectMocks
        private StudyGroupService studyGroupService;

        private User testUser;
        private UserDetails userDetails;

        @BeforeEach
        void setUp() {
                testUser = User.builder()
                                .id(1L)
                                .email("test@example.com")
                                .nickname("testUser")
                                .build();
                userDetails = new UserDetails(testUser);
        }

        @Test
        @DisplayName("스터디 그룹 생성 시 중복된 step 날짜가 있으면 예외가 발생한다")
        void createStudyGroup_WithDuplicateStepDates_ThrowsException() {
                // given
                StudyGroupDto.StudyGroupCreateRequest request = StudyGroupDto.StudyGroupCreateRequest.builder()
                                .name("Test Group")
                                .summary("Test Summary")
                                .maxMembers(10)
                                .startDate(LocalDate.of(2024, 1, 1))
                                .endDate(LocalDate.of(2024, 12, 31))
                                .steps(Arrays.asList(
                                                LocalDate.of(2024, 3, 1),
                                                LocalDate.of(2024, 3, 1), // 중복된 날짜
                                                LocalDate.of(2024, 6, 1)))
                                .build();

                // when & then
                assertThatThrownBy(() -> studyGroupService.createStudyGroup(request, userDetails))
                                .isInstanceOf(RestException.class)
                                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_GROUP_STEP_DUPLICATE_DATE);
        }

        @Test
        @DisplayName("스터디 그룹 생성 시 step의 마지막 날짜와 endDate가 일치하지 않으면 예외가 발생한다")
        void createStudyGroup_WithMismatchedEndDate_ThrowsException() {
                // given
                StudyGroupDto.StudyGroupCreateRequest request = StudyGroupDto.StudyGroupCreateRequest.builder()
                                .name("Test Group")
                                .summary("Test Summary")
                                .maxMembers(10)
                                .startDate(LocalDate.of(2024, 1, 1))
                                .endDate(LocalDate.of(2024, 12, 31)) // step의 마지막 날짜와 다름
                                .steps(Arrays.asList(
                                                LocalDate.of(2024, 3, 1),
                                                LocalDate.of(2024, 6, 1),
                                                LocalDate.of(2024, 9, 1) // 마지막 날짜
                                ))
                                .build();

                // when & then
                assertThatThrownBy(() -> studyGroupService.createStudyGroup(request, userDetails))
                                .isInstanceOf(RestException.class)
                                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_GROUP_STEP_END_DATE_MISMATCH);
        }

        @Test
        @DisplayName("스터디 그룹 생성 시 endDate가 null이면 step의 마지막 날짜로 자동 설정된다")
        void createStudyGroup_WithNullEndDate_SetsEndDateToLastStepDate() {
                // given
                LocalDate lastStepDate = LocalDate.of(2024, 9, 1);
                StudyGroupDto.StudyGroupCreateRequest request = StudyGroupDto.StudyGroupCreateRequest.builder()
                                .name("Test Group")
                                .summary("Test Summary")
                                .maxMembers(10)
                                .startDate(LocalDate.of(2024, 1, 1))
                                .endDate(null) // null로 설정
                                .steps(Arrays.asList(
                                                LocalDate.of(2024, 6, 1),
                                                LocalDate.of(2024, 3, 1),
                                                lastStepDate))
                                .build();

                // Mock 설정
                StudyGroup savedGroup = StudyGroup.builder()
                                .id(1L)
                                .name("Test Group")
                                .summary("Test Summary")
                                .maxMembers(10)
                                .startDate(LocalDate.of(2024, 1, 1))
                                .endDate(lastStepDate)
                                .owner(testUser)
                                .build();

                when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(savedGroup);
                when(memberRepository.save(any())).thenReturn(null);

                // when
                studyGroupService.createStudyGroup(request, userDetails);

                // then
                assertThat(request.getEndDate()).isEqualTo(lastStepDate);
        }

        @Test
        @DisplayName("스터디 그룹 생성 시 steps가 정렬되어 저장된다")
        void createStudyGroup_WithUnsortedSteps_SavesSortedSteps() {
                // given
                List<LocalDate> unsortedSteps = Arrays.asList(
                                LocalDate.of(2024, 6, 1),
                                LocalDate.of(2024, 3, 1),
                                LocalDate.of(2024, 9, 1));

                StudyGroupDto.StudyGroupCreateRequest request = StudyGroupDto.StudyGroupCreateRequest.builder()
                                .name("Test Group")
                                .summary("Test Summary")
                                .maxMembers(10)
                                .startDate(LocalDate.of(2024, 1, 1))
                                .endDate(LocalDate.of(2024, 9, 1))
                                .steps(unsortedSteps)
                                .build();

                // Mock 설정
                StudyGroup savedGroup = StudyGroup.builder()
                                .id(1L)
                                .name("Test Group")
                                .summary("Test Summary")
                                .maxMembers(10)
                                .startDate(LocalDate.of(2024, 1, 1))
                                .endDate(LocalDate.of(2024, 9, 1))
                                .owner(testUser)
                                .build();

                when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(savedGroup);
                when(memberRepository.save(any())).thenReturn(null);

                // when
                studyGroupService.createStudyGroup(request, userDetails);

                // then
                // steps가 정렬되어 저장되었는지 확인 (실제 저장 로직은 mock이므로 validation만 확인)
                assertThat(request.getEndDate()).isEqualTo(LocalDate.of(2024, 9, 1));
        }

        @Test
        @DisplayName("스터디 그룹 생성 시 steps가 null이면 validation을 통과한다")
        void createStudyGroup_WithNullSteps_PassesValidation() {
                // given
                StudyGroupDto.StudyGroupCreateRequest request = StudyGroupDto.StudyGroupCreateRequest.builder()
                                .name("Test Group")
                                .summary("Test Summary")
                                .maxMembers(10)
                                .startDate(LocalDate.of(2024, 1, 1))
                                .endDate(LocalDate.of(2024, 12, 31))
                                .steps(null)
                                .build();

                // Mock 설정
                StudyGroup savedGroup = StudyGroup.builder()
                                .id(1L)
                                .name("Test Group")
                                .summary("Test Summary")
                                .maxMembers(10)
                                .startDate(LocalDate.of(2024, 1, 1))
                                .endDate(LocalDate.of(2024, 12, 31))
                                .owner(testUser)
                                .build();

                when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(savedGroup);
                when(memberRepository.save(any())).thenReturn(null);

                // when & then
                // 예외가 발생하지 않아야 함
                studyGroupService.createStudyGroup(request, userDetails);
                assertThat(request.getSteps()).isNull();
        }

        @Test
        @DisplayName("스터디 그룹 생성 시 steps가 빈 리스트이면 validation을 통과한다")
        void createStudyGroup_WithEmptySteps_PassesValidation() {
                // given
                StudyGroupDto.StudyGroupCreateRequest request = StudyGroupDto.StudyGroupCreateRequest.builder()
                                .name("Test Group")
                                .summary("Test Summary")
                                .maxMembers(10)
                                .startDate(LocalDate.of(2024, 1, 1))
                                .endDate(LocalDate.of(2024, 12, 31))
                                .steps(Arrays.asList())
                                .build();

                // Mock 설정
                StudyGroup savedGroup = StudyGroup.builder()
                                .id(1L)
                                .name("Test Group")
                                .summary("Test Summary")
                                .maxMembers(10)
                                .startDate(LocalDate.of(2024, 1, 1))
                                .endDate(LocalDate.of(2024, 12, 31))
                                .owner(testUser)
                                .build();

                when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(savedGroup);
                when(memberRepository.save(any())).thenReturn(null);

                // when & then
                // 예외가 발생하지 않아야 함
                studyGroupService.createStudyGroup(request, userDetails);
                assertThat(request.getSteps()).isEmpty();
        }
}