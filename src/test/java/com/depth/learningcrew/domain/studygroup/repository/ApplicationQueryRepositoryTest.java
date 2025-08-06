package com.depth.learningcrew.domain.studygroup.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.depth.learningcrew.domain.studygroup.dto.ApplicationDto;
import com.depth.learningcrew.domain.studygroup.entity.Application;
import com.depth.learningcrew.domain.studygroup.entity.ApplicationId;
import com.depth.learningcrew.domain.studygroup.entity.GroupCategory;
import com.depth.learningcrew.domain.studygroup.entity.State;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.user.entity.Gender;
import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.domain.user.repository.UserRepository;
import com.depth.learningcrew.system.security.model.UserDetails;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ApplicationQueryRepositoryTest {

    @Autowired
    private ApplicationQueryRepository applicationQueryRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupCategoryRepository groupCategoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User owner;
    private User applicant1;
    private User applicant2;
    private User applicant3;
    private StudyGroup studyGroup;
    private GroupCategory category;

    @BeforeEach
    void setUp() {
        // 사용자 생성
        owner = User.builder()
                .nickname("스터디장")
                .email("owner@test.com")
                .password("password")
                .role(Role.USER)
                .gender(Gender.MALE)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        userRepository.save(owner);

        applicant1 = User.builder()
                .nickname("신청자1")
                .email("applicant1@test.com")
                .password("password")
                .role(Role.USER)
                .gender(Gender.FEMALE)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        userRepository.save(applicant1);

        applicant2 = User.builder()
                .nickname("신청자2")
                .email("applicant2@test.com")
                .password("password")
                .role(Role.USER)
                .gender(Gender.MALE)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        userRepository.save(applicant2);

        applicant3 = User.builder()
                .nickname("김신청")
                .email("applicant3@test.com")
                .password("password")
                .role(Role.USER)
                .gender(Gender.FEMALE)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        userRepository.save(applicant3);

        // 카테고리 생성
        category = GroupCategory.builder()
                .name("프로그래밍")
                .build();
        groupCategoryRepository.save(category);

        // 스터디 그룹 생성
        studyGroup = StudyGroup.builder()
                .name("테스트 스터디")
                .summary("테스트용 스터디입니다.")
                .content("테스트용 스터디 상세 내용입니다.")
                .owner(owner)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(2))
                .memberCount(1)
                .maxMembers(10)
                .currentStep(1)
                .categories(List.of(category))
                .build();
        studyGroupRepository.save(studyGroup);

        // 가입 신청 생성
        Application application1 = Application.builder()
                .id(ApplicationId.of(applicant1, studyGroup))
                .state(State.PENDING)
                .build();
        applicationRepository.save(application1);

        Application application2 = Application.builder()
                .id(ApplicationId.of(applicant2, studyGroup))
                .state(State.APPROVED)
                .build();
        applicationRepository.save(application2);

        Application application3 = Application.builder()
                .id(ApplicationId.of(applicant3, studyGroup))
                .state(State.PENDING)
                .build();
        applicationRepository.save(application3);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("스터디 그룹의 모든 가입 신청을 페이지네이션으로 조회할 수 있다")
    void paginateApplicationsByGroupId() {
        // given
        UserDetails userDetails = new UserDetails(owner);
        ApplicationDto.SearchConditions searchConditions = ApplicationDto.SearchConditions.builder().build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ApplicationDto.ApplicationResponse> result = applicationQueryRepository
                .paginateApplicationsByGroupId(studyGroup.getId(), searchConditions, userDetails, pageable);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("상태별로 가입 신청을 필터링할 수 있다")
    void filterByState() {
        // given
        UserDetails userDetails = new UserDetails(owner);
        ApplicationDto.SearchConditions searchConditions = ApplicationDto.SearchConditions.builder()
                .state(State.PENDING)
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ApplicationDto.ApplicationResponse> result = applicationQueryRepository
                .paginateApplicationsByGroupId(studyGroup.getId(), searchConditions, userDetails, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(app -> app.getState() == State.PENDING);
    }

    @Test
    @DisplayName("신청자 이름으로 검색할 수 있다")
    void searchByName() {
        // given
        UserDetails userDetails = new UserDetails(owner);
        ApplicationDto.SearchConditions searchConditions = ApplicationDto.SearchConditions.builder()
                .keyword("김신청")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ApplicationDto.ApplicationResponse> result = applicationQueryRepository
                .paginateApplicationsByGroupId(studyGroup.getId(), searchConditions, userDetails, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUser().getNickname()).isEqualTo("김신청");
    }

    @Test
    @DisplayName("생성일 기준으로 정렬할 수 있다")
    void sortByCreatedAt() {
        // given
        UserDetails userDetails = new UserDetails(owner);
        ApplicationDto.SearchConditions searchConditions = ApplicationDto.SearchConditions.builder()
                .sort("created_at")
                .order("asc")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ApplicationDto.ApplicationResponse> result = applicationQueryRepository
                .paginateApplicationsByGroupId(studyGroup.getId(), searchConditions, userDetails, pageable);

        // then
        assertThat(result.getContent()).hasSize(3);
        // 생성일 순서대로 정렬되었는지 확인
        List<ApplicationDto.ApplicationResponse> content = result.getContent();
        assertThat(content.get(0).getCreatedAt()).isBeforeOrEqualTo(content.get(1).getCreatedAt());
        assertThat(content.get(1).getCreatedAt()).isBeforeOrEqualTo(content.get(2).getCreatedAt());
    }

    @Test
    @DisplayName("알파벳 순으로 정렬할 수 있다")
    void sortByAlphabet() {
        // given
        UserDetails userDetails = new UserDetails(owner);
        ApplicationDto.SearchConditions searchConditions = ApplicationDto.SearchConditions.builder()
                .sort("alphabet")
                .order("asc")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<ApplicationDto.ApplicationResponse> result = applicationQueryRepository
                .paginateApplicationsByGroupId(studyGroup.getId(), searchConditions, userDetails, pageable);

        // then
        assertThat(result.getContent()).hasSize(3);
        // 알파벳 순서대로 정렬되었는지 확인
        List<ApplicationDto.ApplicationResponse> content = result.getContent();
        assertThat(content.get(0).getUser().getNickname()).isEqualTo("김신청");
        assertThat(content.get(1).getUser().getNickname()).isEqualTo("신청자1");
        assertThat(content.get(2).getUser().getNickname()).isEqualTo("신청자2");
    }
} 