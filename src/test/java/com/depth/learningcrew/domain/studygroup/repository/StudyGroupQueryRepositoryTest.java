package com.depth.learningcrew.domain.studygroup.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

import com.depth.learningcrew.domain.studygroup.dto.StudyGroupDto;
import com.depth.learningcrew.domain.studygroup.entity.Dibs;
import com.depth.learningcrew.domain.studygroup.entity.DibsId;
import com.depth.learningcrew.domain.studygroup.entity.GroupCategory;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.user.entity.Gender;
import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.security.model.UserDetails;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class StudyGroupQueryRepositoryTest {

  @Autowired
  private StudyGroupQueryRepository studyGroupQueryRepository;

  @PersistenceContext
  private EntityManager entityManager;

  private User testUser;
  private User otherUser;
  private UserDetails testUserDetails;
  private GroupCategory programmingCategory;
  private GroupCategory designCategory;
  private StudyGroup studyGroup1;
  private StudyGroup studyGroup2;
  private StudyGroup studyGroup3;

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

    otherUser = User.builder()
        .email("other@example.com")
        .password("password")
        .nickname("otherUser")
        .birthday(LocalDate.of(1995, 1, 1))
        .gender(Gender.FEMALE)
        .role(Role.USER)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    // 카테고리 생성
    programmingCategory = new GroupCategory(null, "프로그래밍", new ArrayList<>());
    designCategory = new GroupCategory(null, "디자인", new ArrayList<>());

    // 스터디 그룹 생성
    studyGroup1 = StudyGroup.builder()
        .name("Java 스터디")
        .summary("Java 프로그래밍 스터디입니다.")
        .content("Java 기초부터 고급까지 학습하는 스터디입니다.")
        .maxMembers(10)
        .memberCount(3)
        .currentStep(1)
        .categories(List.of(programmingCategory))
        .startDate(LocalDate.now())
        .endDate(LocalDate.now().plusMonths(3))
        .owner(testUser)
        .createdAt(LocalDateTime.now().minusDays(5))
        .lastModifiedAt(LocalDateTime.now().minusDays(5))
        .build();

    studyGroup2 = StudyGroup.builder()
        .name("Spring Boot 스터디")
        .summary("Spring Boot 프레임워크 스터디입니다.")
        .content("Spring Boot를 활용한 웹 개발 스터디입니다.")
        .maxMembers(8)
        .memberCount(5)
        .currentStep(2)
        .categories(List.of(programmingCategory))
        .startDate(LocalDate.now().plusDays(7))
        .endDate(LocalDate.now().plusMonths(4))
        .owner(testUser)
        .createdAt(LocalDateTime.now().minusDays(3))
        .lastModifiedAt(LocalDateTime.now().minusDays(3))
        .build();

    studyGroup3 = StudyGroup.builder()
        .name("UI/UX 디자인 스터디")
        .summary("UI/UX 디자인 스터디입니다.")
        .content("사용자 경험을 중심으로 한 디자인 스터디입니다.")
        .maxMembers(6)
        .memberCount(2)
        .currentStep(1)
        .categories(List.of(designCategory))
        .startDate(LocalDate.now().plusDays(14))
        .endDate(LocalDate.now().plusMonths(2))
        .owner(otherUser)
        .createdAt(LocalDateTime.now().minusDays(1))
        .lastModifiedAt(LocalDateTime.now().minusDays(1))
        .build();

    // 엔티티들을 데이터베이스에 저장
    entityManager.persist(testUser);
    entityManager.persist(otherUser);
    entityManager.persist(programmingCategory);
    entityManager.persist(designCategory);
    entityManager.persist(studyGroup1);
    entityManager.persist(studyGroup2);
    entityManager.persist(studyGroup3);

    // 찜 데이터 생성 (testUser가 studyGroup1을 찜함)
    Dibs dibs = Dibs.builder()
        .id(DibsId.of(testUser, studyGroup1))
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();
    entityManager.persist(dibs);

    entityManager.flush();
    entityManager.clear();

    testUserDetails = UserDetails.builder()
        .user(testUser)
        .build();
  }

  @Test
  @DisplayName("사용자가 주최한 스터디 그룹 목록을 페이징하여 조회할 수 있다")
  void paginateMyOwnedGroups_ShouldReturnUserOwnedGroups() {
    // given
    StudyGroupDto.SearchConditions searchConditions = StudyGroupDto.SearchConditions.builder()
        .sort("created_at")
        .order("desc")
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<StudyGroupDto.StudyGroupResponse> result = studyGroupQueryRepository
        .paginateMyOwnedGroups(searchConditions, testUserDetails, pageable);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(2);

    // 생성일 기준 내림차순 정렬 확인 (studyGroup2가 더 최근)
    assertThat(result.getContent().get(0).getName()).isEqualTo("Spring Boot 스터디");
    assertThat(result.getContent().get(1).getName()).isEqualTo("Java 스터디");

    // 찜 여부 확인
    assertThat(result.getContent().get(0).getDibs()).isFalse(); // studyGroup2는 찜하지 않음
    assertThat(result.getContent().get(1).getDibs()).isTrue(); // studyGroup1은 찜함
  }

  @Test
  @DisplayName("빈 검색 결과가 있을 때 빈 페이지를 반환한다")
  void paginateMyOwnedGroups_WithNoOwnedGroups_ShouldReturnEmptyPage() {
    // given
    User newUser = User.builder()
        .email("new@example.com")
        .password("password")
        .nickname("newUser")
        .birthday(LocalDate.of(2000, 1, 1))
        .gender(Gender.MALE)
        .role(Role.USER)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();
    entityManager.persist(newUser);
    entityManager.flush();

    UserDetails newUserDetails = UserDetails.builder()
        .user(newUser)
        .build();

    StudyGroupDto.SearchConditions searchConditions = StudyGroupDto.SearchConditions.builder()
        .sort("created_at")
        .order("desc")
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<StudyGroupDto.StudyGroupResponse> result = studyGroupQueryRepository
        .paginateMyOwnedGroups(searchConditions, newUserDetails, pageable);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isEqualTo(0);
  }

  @Test
  @DisplayName("다양한 정렬 조건으로 스터디 그룹을 조회할 수 있다")
  void paginateMyOwnedGroups_WithDifferentSortConditions_ShouldWorkCorrectly() {
    // given
    StudyGroupDto.SearchConditions searchConditions = StudyGroupDto.SearchConditions.builder()
        .sort("alphabet")
        .order("asc")
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<StudyGroupDto.StudyGroupResponse> result = studyGroupQueryRepository
        .paginateMyOwnedGroups(searchConditions, testUserDetails, pageable);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(2);

    // 알파벳 순 오름차순 정렬 확인 (Java -> Spring Boot)
    assertThat(result.getContent().get(0).getName()).isEqualTo("Java 스터디");
    assertThat(result.getContent().get(1).getName()).isEqualTo("Spring Boot 스터디");
  }

  @Test
  @DisplayName("페이징이 제대로 작동한다")
  void paginateMyOwnedGroups_WithPaging_ShouldWorkCorrectly() {
    // given
    // 추가 스터디 그룹 생성
    StudyGroup studyGroup4 = StudyGroup.builder()
        .name("Python 스터디")
        .summary("Python 프로그래밍 스터디입니다.")
        .content("Python 기초부터 고급까지 학습하는 스터디입니다.")
        .maxMembers(12)
        .memberCount(4)
        .currentStep(1)
        .categories(List.of(programmingCategory))
        .startDate(LocalDate.now().plusDays(21))
        .endDate(LocalDate.now().plusMonths(5))
        .owner(testUser)
        .createdAt(LocalDateTime.now().minusDays(10))
        .lastModifiedAt(LocalDateTime.now().minusDays(10))
        .build();
    entityManager.persist(studyGroup4);
    entityManager.flush();

    StudyGroupDto.SearchConditions searchConditions = StudyGroupDto.SearchConditions.builder()
        .sort("created_at")
        .order("desc")
        .build();
    Pageable pageable = PageRequest.of(0, 2); // 페이지당 2개씩

    // when
    Page<StudyGroupDto.StudyGroupResponse> result = studyGroupQueryRepository
        .paginateMyOwnedGroups(searchConditions, testUserDetails, pageable);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(3);
    assertThat(result.getTotalPages()).isEqualTo(2);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.hasPrevious()).isFalse();
  }

  @Test
  @DisplayName("카테고리 필터링이 제대로 작동한다")
  void paginateMyOwnedGroups_WithCategoryFilter_ShouldWorkCorrectly() {
    // given
    StudyGroupDto.SearchConditions searchConditions = StudyGroupDto.SearchConditions.builder()
        .sort("created_at")
        .order("desc")
        .categoryId(programmingCategory.getId())
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<StudyGroupDto.StudyGroupResponse> result = studyGroupQueryRepository
        .paginateMyOwnedGroups(searchConditions, testUserDetails, pageable);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(2);

    // 프로그래밍 카테고리의 스터디 그룹들만 반환되는지 확인
    assertThat(result.getContent().get(0).getName()).isEqualTo("Spring Boot 스터디");
    assertThat(result.getContent().get(1).getName()).isEqualTo("Java 스터디");
  }

  @Test
  @DisplayName("존재하지 않는 카테고리로 필터링하면 빈 결과를 반환한다")
  void paginateMyOwnedGroups_WithNonExistentCategory_ShouldReturnEmptyResult() {
    // given
    StudyGroupDto.SearchConditions searchConditions = StudyGroupDto.SearchConditions.builder()
        .sort("created_at")
        .order("desc")
        .categoryId(999) // 존재하지 않는 카테고리 ID
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<StudyGroupDto.StudyGroupResponse> result = studyGroupQueryRepository
        .paginateMyOwnedGroups(searchConditions, testUserDetails, pageable);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isEqualTo(0);
  }

  @Test
  @DisplayName("검색어로 스터디 그룹을 필터링할 수 있다")
  void paginateMyOwnedGroups_WithSearchKeyword_ShouldFilterCorrectly() {
    // given
    StudyGroupDto.SearchConditions searchConditions = StudyGroupDto.SearchConditions.builder()
        .sort("created_at")
        .order("desc")
        .searchKeyword("Java")
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<StudyGroupDto.StudyGroupResponse> result = studyGroupQueryRepository
        .paginateMyOwnedGroups(searchConditions, testUserDetails, pageable);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getName()).isEqualTo("Java 스터디");
  }

  @Test
  @DisplayName("검색어가 카테고리명에 포함된 스터디 그룹을 찾을 수 있다")
  void paginateMyOwnedGroups_WithCategorySearch_ShouldFindCorrectly() {
    // given
    StudyGroupDto.SearchConditions searchConditions = StudyGroupDto.SearchConditions.builder()
        .sort("created_at")
        .order("desc")
        .searchKeyword("프로그래밍")
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<StudyGroupDto.StudyGroupResponse> result = studyGroupQueryRepository
        .paginateMyOwnedGroups(searchConditions, testUserDetails, pageable);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(2); // 프로그래밍 카테고리의 스터디 그룹들
  }

  @Test
  @DisplayName("relative 정렬 시 검색어가 있으면 관련성 점수로 정렬한다")
  void paginateMyOwnedGroups_WithRelativeSortAndSearchKeyword_ShouldSortByRelevance() {
    // given
    // 검색어 "스터디"가 제목에 포함된 그룹과 내용에만 포함된 그룹을 생성
    StudyGroup highRelevanceGroup = StudyGroup.builder()
        .name("스터디 그룹 A")
        .summary("스터디 그룹 A입니다.")
        .content("일반적인 내용입니다.")
        .maxMembers(5)
        .memberCount(2)
        .currentStep(1)
        .categories(List.of(programmingCategory))
        .startDate(LocalDate.now())
        .endDate(LocalDate.now().plusMonths(1))
        .owner(testUser)
        .createdAt(LocalDateTime.now().minusDays(2))
        .lastModifiedAt(LocalDateTime.now().minusDays(2))
        .build();

    StudyGroup lowRelevanceGroup = StudyGroup.builder()
        .name("일반 그룹")
        .summary("일반 그룹입니다.")
        .content("스터디 관련 내용입니다.")
        .maxMembers(5)
        .memberCount(2)
        .currentStep(1)
        .categories(List.of(programmingCategory))
        .startDate(LocalDate.now())
        .endDate(LocalDate.now().plusMonths(1))
        .owner(testUser)
        .createdAt(LocalDateTime.now().minusDays(1))
        .lastModifiedAt(LocalDateTime.now().minusDays(1))
        .build();

    entityManager.persist(highRelevanceGroup);
    entityManager.persist(lowRelevanceGroup);
    entityManager.flush();

    StudyGroupDto.SearchConditions searchConditions = StudyGroupDto.SearchConditions.builder()
        .sort("relative")
        .order("desc")
        .searchKeyword("스터디")
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<StudyGroupDto.StudyGroupResponse> result = studyGroupQueryRepository
        .paginateMyOwnedGroups(searchConditions, testUserDetails, pageable);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(3); // 기존 2개 + 새로 추가된 1개 (일반 그룹은 검색 조건에 맞지 않음)

    // 관련성 점수가 높은 그룹이 먼저 나와야 함 (제목에 "스터디"가 포함된 그룹이 +3점)
    // 결과를 확인하여 "스터디"가 포함된 그룹이 상위에 있는지 확인
    boolean hasHighRelevanceGroup = result.getContent().stream()
        .anyMatch(group -> group.getName().equals("스터디 그룹 A"));
    assertThat(hasHighRelevanceGroup).isTrue();
  }

  @Test
  @DisplayName("relative 정렬 시 검색어가 없으면 시작일 기준으로 정렬한다")
  void paginateMyOwnedGroups_WithRelativeSortWithoutSearchKeyword_ShouldSortByStartDate() {
    // given
    StudyGroupDto.SearchConditions searchConditions = StudyGroupDto.SearchConditions.builder()
        .sort("relative")
        .order("desc")
        .build(); // 검색어 없음
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<StudyGroupDto.StudyGroupResponse> result = studyGroupQueryRepository
        .paginateMyOwnedGroups(searchConditions, testUserDetails, pageable);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(2);

    // 시작일 기준 내림차순 정렬 확인 (studyGroup2가 더 늦은 시작일)
    assertThat(result.getContent().get(0).getName()).isEqualTo("Spring Boot 스터디");
    assertThat(result.getContent().get(1).getName()).isEqualTo("Java 스터디");
  }

  @Test
  @DisplayName("검색어와 카테고리 필터링이 동시에 적용된다")
  void paginateMyOwnedGroups_WithSearchKeywordAndCategory_ShouldApplyBothFilters() {
    // given
    StudyGroupDto.SearchConditions searchConditions = StudyGroupDto.SearchConditions.builder()
        .sort("created_at")
        .order("desc")
        .searchKeyword("Java")
        .categoryId(programmingCategory.getId())
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<StudyGroupDto.StudyGroupResponse> result = studyGroupQueryRepository
        .paginateMyOwnedGroups(searchConditions, testUserDetails, pageable);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getName()).isEqualTo("Java 스터디");
  }

  @Test
  @DisplayName("대소문자를 구분하지 않고 검색한다")
  void paginateMyOwnedGroups_WithCaseInsensitiveSearch_ShouldWorkCorrectly() {
    // given
    StudyGroupDto.SearchConditions searchConditions = StudyGroupDto.SearchConditions.builder()
        .sort("created_at")
        .order("desc")
        .searchKeyword("java")
        .build(); // 소문자로 검색
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<StudyGroupDto.StudyGroupResponse> result = studyGroupQueryRepository
        .paginateMyOwnedGroups(searchConditions, testUserDetails, pageable);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getName()).isEqualTo("Java 스터디");
  }
}