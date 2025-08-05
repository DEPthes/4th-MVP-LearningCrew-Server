package com.depth.learningcrew.domain.studygroup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

import com.depth.learningcrew.domain.studygroup.dto.StudyGroupDto;
import com.depth.learningcrew.domain.studygroup.entity.Dibs;
import com.depth.learningcrew.domain.studygroup.entity.DibsId;
import com.depth.learningcrew.domain.studygroup.entity.GroupCategory;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.entity.StudyStep;
import com.depth.learningcrew.domain.studygroup.entity.StudyStepId;
import com.depth.learningcrew.domain.studygroup.repository.DibsRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupQueryRepository;
import com.depth.learningcrew.domain.user.entity.Gender;
import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.security.model.UserDetails;
import org.springframework.data.web.PagedModel;

@ExtendWith(MockitoExtension.class)
class StudyGroupServiceTest {

        @Mock
        private StudyGroupQueryRepository studyGroupQueryRepository;

        @Mock
        private DibsRepository dibsRepository;

        @InjectMocks
        private StudyGroupService studyGroupService;

        private User testUser;
        private UserDetails testUserDetails;
        private StudyGroupDto.SearchConditions searchConditions;
        private Pageable pageable;
        private StudyGroup testStudyGroup;
        private StudyGroupDto.StudyGroupResponse testResponse;

        @BeforeEach
        void setUp() {
                // 테스트 사용자 설정
                testUser = User.builder()
                                .id(1L)
                                .email("test@example.com")
                                .password("password")
                                .nickname("testUser")
                                .birthday(LocalDate.of(1990, 1, 1))
                                .gender(Gender.MALE)
                                .role(Role.USER)
                                .createdAt(LocalDateTime.now())
                                .lastModifiedAt(LocalDateTime.now())
                                .build();

                testUserDetails = UserDetails.builder()
                                .user(testUser)
                                .build();

                // 테스트 스터디 그룹 설정
                testStudyGroup = StudyGroup.builder()
                                .id(1L)
                                .name("테스트 스터디 그룹")
                                .summary("테스트 스터디 그룹입니다.")
                                .maxMembers(10)
                                .startDate(LocalDate.now())
                                .endDate(LocalDate.now().plusMonths(3))
                                .owner(testUser)
                                .createdAt(LocalDateTime.now())
                                .lastModifiedAt(LocalDateTime.now())
                                .build();

                // 테스트 응답 DTO 설정
                testResponse = StudyGroupDto.StudyGroupResponse.builder()
                                .id(testStudyGroup.getId())
                                .name(testStudyGroup.getName())
                                .summary(testStudyGroup.getSummary())
                                .maxMembers(testStudyGroup.getMaxMembers())
                                .startDate(testStudyGroup.getStartDate())
                                .endDate(testStudyGroup.getEndDate())
                                .owner(null) // UserDto.UserResponse.from() 호출 시점에 설정
                                .createdAt(testStudyGroup.getCreatedAt())
                                .lastModifiedAt(testStudyGroup.getLastModifiedAt())
                                .memberCount(3)
                                .dibs(false)
                                .build();

                // 검색 조건 설정
                searchConditions = StudyGroupDto.SearchConditions.builder()
                                .sort("created_at")
                                .order("desc")
                                .categoryId(null)
                                .searchKeyword(null)
                                .build();

                // 페이징 설정
                pageable = PageRequest.of(0, 10);
        }

        @Test
        @DisplayName("내가 주최한 스터디 그룹 목록을 페이징하여 조회할 수 있다")
        void paginateMyOwnedStudyGroups_ShouldReturnPagedResults() {
                // given
                Page<StudyGroupDto.StudyGroupResponse> mockPage = new PageImpl<>(
                                List.of(testResponse), pageable, 1L);

                when(studyGroupQueryRepository.paginateMyOwnedGroups(
                                eq(searchConditions), eq(testUserDetails), eq(pageable)))
                                .thenReturn(mockPage);

                // when
                PagedModel<StudyGroupDto.StudyGroupResponse> result = studyGroupService
                                .paginateMyOwnedStudyGroups(searchConditions, testUserDetails, pageable);

                // then
                verify(studyGroupQueryRepository, times(1))
                                .paginateMyOwnedGroups(eq(searchConditions), eq(testUserDetails), eq(pageable));

                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(1);
                assertThat(result.getContent().get(0).getId()).isEqualTo(testStudyGroup.getId());
                assertThat(result.getContent().get(0).getName()).isEqualTo(testStudyGroup.getName());
                assertThat(result.getContent().get(0).getSummary()).isEqualTo(testStudyGroup.getSummary());
                assertThat(result.getContent().get(0).getMaxMembers()).isEqualTo(testStudyGroup.getMaxMembers());
        }

        @Test
        @DisplayName("빈 검색 결과가 있을 때도 정상적으로 PagedModel을 반환한다")
        void paginateMyOwnedStudyGroups_WithEmptyResults_ShouldReturnEmptyPagedModel() {
                // given
                Page<StudyGroupDto.StudyGroupResponse> emptyPage = new PageImpl<>(
                                List.of(), pageable, 0L);

                when(studyGroupQueryRepository.paginateMyOwnedGroups(
                                eq(searchConditions), eq(testUserDetails), eq(pageable)))
                                .thenReturn(emptyPage);

                // when
                PagedModel<StudyGroupDto.StudyGroupResponse> result = studyGroupService
                                .paginateMyOwnedStudyGroups(searchConditions, testUserDetails, pageable);

                // then
                verify(studyGroupQueryRepository, times(1))
                                .paginateMyOwnedGroups(eq(searchConditions), eq(testUserDetails), eq(pageable));

                assertThat(result).isNotNull();
                assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("다양한 검색 조건으로 스터디 그룹을 조회할 수 있다")
        void paginateMyOwnedStudyGroups_WithDifferentSearchConditions_ShouldWorkCorrectly() {
                // given
                StudyGroupDto.SearchConditions customSearchConditions = StudyGroupDto.SearchConditions.builder()
                                .sort("alphabet")
                                .order("asc")
                                .categoryId(1)
                                .build();

                Page<StudyGroupDto.StudyGroupResponse> mockPage = new PageImpl<>(
                                List.of(testResponse), pageable, 1L);

                when(studyGroupQueryRepository.paginateMyOwnedGroups(
                                eq(customSearchConditions), eq(testUserDetails), eq(pageable)))
                                .thenReturn(mockPage);

                // when
                PagedModel<StudyGroupDto.StudyGroupResponse> result = studyGroupService
                                .paginateMyOwnedStudyGroups(customSearchConditions, testUserDetails, pageable);

                // then
                verify(studyGroupQueryRepository, times(1))
                                .paginateMyOwnedGroups(eq(customSearchConditions), eq(testUserDetails), eq(pageable));

                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("다양한 페이징 설정으로 스터디 그룹을 조회할 수 있다")
        void paginateMyOwnedStudyGroups_WithDifferentPageable_ShouldWorkCorrectly() {
                // given
                Pageable customPageable = PageRequest.of(1, 5); // 두 번째 페이지, 5개씩

                Page<StudyGroupDto.StudyGroupResponse> mockPage = new PageImpl<>(
                                List.of(testResponse), customPageable, 1L);

                when(studyGroupQueryRepository.paginateMyOwnedGroups(
                                eq(searchConditions), eq(testUserDetails), eq(customPageable)))
                                .thenReturn(mockPage);

                // when
                PagedModel<StudyGroupDto.StudyGroupResponse> result = studyGroupService
                                .paginateMyOwnedStudyGroups(searchConditions, testUserDetails, customPageable);

                // then
                verify(studyGroupQueryRepository, times(1))
                                .paginateMyOwnedGroups(eq(searchConditions), eq(testUserDetails), eq(customPageable));

                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("여러 개의 스터디 그룹이 있을 때 모든 결과를 반환한다")
        void paginateMyOwnedStudyGroups_WithMultipleResults_ShouldReturnAllResults() {
                // given
                StudyGroupDto.StudyGroupResponse secondResponse = StudyGroupDto.StudyGroupResponse.builder()
                                .id(2L)
                                .name("두 번째 스터디 그룹")
                                .summary("두 번째 스터디 그룹입니다.")
                                .maxMembers(5)
                                .startDate(LocalDate.now())
                                .endDate(LocalDate.now().plusMonths(2))
                                .createdAt(LocalDateTime.now())
                                .lastModifiedAt(LocalDateTime.now())
                                .memberCount(2)
                                .dibs(true)
                                .build();

                Page<StudyGroupDto.StudyGroupResponse> mockPage = new PageImpl<>(
                                List.of(testResponse, secondResponse), pageable, 2L);

                when(studyGroupQueryRepository.paginateMyOwnedGroups(
                                eq(searchConditions), eq(testUserDetails), eq(pageable)))
                                .thenReturn(mockPage);

                // when
                PagedModel<StudyGroupDto.StudyGroupResponse> result = studyGroupService
                                .paginateMyOwnedStudyGroups(searchConditions, testUserDetails, pageable);

                // then
                verify(studyGroupQueryRepository, times(1))
                                .paginateMyOwnedGroups(eq(searchConditions), eq(testUserDetails), eq(pageable));

                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(2);
                assertThat(result.getContent().get(0).getId()).isEqualTo(1);
                assertThat(result.getContent().get(1).getId()).isEqualTo(2);
                assertThat(result.getContent().get(0).getDibs()).isFalse();
                assertThat(result.getContent().get(1).getDibs()).isTrue();
        }

        @Test
        @DisplayName("검색어가 포함된 검색 조건으로 스터디 그룹을 조회할 수 있다")
        void paginateMyOwnedStudyGroups_WithSearchKeyword_ShouldWorkCorrectly() {
                // given
                StudyGroupDto.SearchConditions searchConditionsWithKeyword = StudyGroupDto.SearchConditions.builder()
                                .sort("created_at")
                                .order("desc")
                                .searchKeyword("테스트")
                                .build();

                Page<StudyGroupDto.StudyGroupResponse> mockPage = new PageImpl<>(
                                List.of(testResponse), pageable, 1L);

                when(studyGroupQueryRepository.paginateMyOwnedGroups(
                                eq(searchConditionsWithKeyword), eq(testUserDetails), eq(pageable)))
                                .thenReturn(mockPage);

                // when
                PagedModel<StudyGroupDto.StudyGroupResponse> result = studyGroupService
                                .paginateMyOwnedStudyGroups(searchConditionsWithKeyword, testUserDetails, pageable);

                // then
                verify(studyGroupQueryRepository, times(1))
                                .paginateMyOwnedGroups(eq(searchConditionsWithKeyword), eq(testUserDetails),
                                                eq(pageable));

                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("relative 정렬과 검색어가 포함된 검색 조건으로 스터디 그룹을 조회할 수 있다")
        void paginateMyOwnedStudyGroups_WithRelativeSortAndSearchKeyword_ShouldWorkCorrectly() {
                // given
                StudyGroupDto.SearchConditions searchConditionsWithRelativeSort = StudyGroupDto.SearchConditions
                                .builder()
                                .sort("relative")
                                .order("desc")
                                .searchKeyword("스터디")
                                .build();

                Page<StudyGroupDto.StudyGroupResponse> mockPage = new PageImpl<>(
                                List.of(testResponse), pageable, 1L);

                when(studyGroupQueryRepository.paginateMyOwnedGroups(
                                eq(searchConditionsWithRelativeSort), eq(testUserDetails), eq(pageable)))
                                .thenReturn(mockPage);

                // when
                PagedModel<StudyGroupDto.StudyGroupResponse> result = studyGroupService
                                .paginateMyOwnedStudyGroups(searchConditionsWithRelativeSort, testUserDetails,
                                                pageable);

                // then
                verify(studyGroupQueryRepository, times(1))
                                .paginateMyOwnedGroups(eq(searchConditionsWithRelativeSort), eq(testUserDetails),
                                                eq(pageable));

                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("검색어, 카테고리, 정렬 조건이 모두 포함된 검색 조건으로 스터디 그룹을 조회할 수 있다")
        void paginateMyOwnedStudyGroups_WithAllSearchConditions_ShouldWorkCorrectly() {
                // given
                StudyGroupDto.SearchConditions allSearchConditions = StudyGroupDto.SearchConditions.builder()
                                .sort("alphabet")
                                .order("asc")
                                .categoryId(1)
                                .searchKeyword("테스트")
                                .build();

                Page<StudyGroupDto.StudyGroupResponse> mockPage = new PageImpl<>(
                                List.of(testResponse), pageable, 1L);

                when(studyGroupQueryRepository.paginateMyOwnedGroups(
                                eq(allSearchConditions), eq(testUserDetails), eq(pageable)))
                                .thenReturn(mockPage);

                // when
                PagedModel<StudyGroupDto.StudyGroupResponse> result = studyGroupService
                                .paginateMyOwnedStudyGroups(allSearchConditions, testUserDetails, pageable);

                // then
                verify(studyGroupQueryRepository, times(1))
                                .paginateMyOwnedGroups(eq(allSearchConditions), eq(testUserDetails), eq(pageable));

                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("스터디 그룹 상세 정보를 조회할 수 있다")
        void getStudyGroupDetail_ShouldReturnDetailResponse() throws Exception {

                // given
                GroupCategory category = GroupCategory.builder()
                                .id(1)
                                .name("테스트 카테고리")
                                .build();

                StudyGroup studyGroup = StudyGroup.builder()
                                .id(1L)
                                .name("스터디 그룹")
                                .summary("스터디그룹 소개")
                                .content("스터디 내용")
                                .maxMembers(10)
                                .memberCount(3)
                                .currentStep(1)
                                .owner(testUser)
                                .startDate(LocalDate.now())
                                .endDate(LocalDate.now().plusMonths(1))
                                .build();

                StudyStep step1 = StudyStep.builder()
                                .id(StudyStepId.of(1, studyGroup))
                                .endDate(LocalDate.now().plusWeeks(1))
                                .build();

                StudyStep step2 = StudyStep.builder()
                                .id(StudyStepId.of(2, studyGroup))
                                .endDate(LocalDate.now().plusWeeks(2))
                                .build();

                Dibs dibs = Dibs.builder()
                                .id(DibsId.of(testUser, studyGroup))
                                .build();

                studyGroup.getSteps().addAll(List.of(step1, step2));
                studyGroup.getCategories().add(category);

                when(studyGroupQueryRepository.findDetailById(1L)).thenReturn(Optional.of(studyGroup));
                when(dibsRepository.existsById_UserAndId_StudyGroup(testUser, studyGroup)).thenReturn(true);

                // when
                StudyGroupDto.StudyGroupDetailResponse result = studyGroupService.getStudyGroupDetail(1L,
                                testUserDetails);

                // then
                assertThat(result.getId()).isEqualTo(studyGroup.getId());
                assertThat(result.getName()).isEqualTo("스터디 그룹");
                assertThat(result.getDibs()).isTrue();
                assertThat(result.getSteps()).hasSize(2);
                assertThat(result.getCategories().get(0).getName()).isEqualTo("테스트 카테고리");

        }
}