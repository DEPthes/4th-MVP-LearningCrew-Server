package com.depth.learningcrew.domain.studygroup.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

import com.depth.learningcrew.domain.studygroup.dto.MemberDto;
import com.depth.learningcrew.domain.studygroup.entity.Member;
import com.depth.learningcrew.domain.studygroup.entity.MemberId;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.user.entity.Gender;
import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.domain.user.entity.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MemberQueryRepositoryTest {

    @Autowired
    private MemberQueryRepository memberQueryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User owner;
    private User member1;
    private User member2;
    private User member3;
    private StudyGroup studyGroup;
    private Member memberEntity1;
    private Member memberEntity2;
    private Member memberEntity3;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        owner = User.builder()
                .email("owner@example.com")
                .password("password")
                .nickname("owner")
                .birthday(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        member1 = User.builder()
                .email("member1@example.com")
                .password("password")
                .nickname("alice")
                .birthday(LocalDate.of(1991, 1, 1))
                .gender(Gender.FEMALE)
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        member2 = User.builder()
                .email("member2@example.com")
                .password("password")
                .nickname("bob")
                .birthday(LocalDate.of(1992, 1, 1))
                .gender(Gender.MALE)
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        member3 = User.builder()
                .email("member3@example.com")
                .password("password")
                .nickname("charlie")
                .birthday(LocalDate.of(1993, 1, 1))
                .gender(Gender.MALE)
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        // 스터디 그룹 생성
        studyGroup = StudyGroup.builder()
                .name("테스트 스터디 그룹")
                .summary("테스트 스터디 그룹입니다.")
                .maxMembers(10)
                .memberCount(3)
                .currentStep(1)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        // 엔티티들을 데이터베이스에 저장
        entityManager.persist(owner);
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(member3);
        entityManager.persist(studyGroup);
        entityManager.flush();

        // 멤버 엔티티 생성 및 저장 (감사 타임스탬프가 적용되도록 persist 간 간격을 둠)
        memberEntity1 = Member.builder()
                .id(MemberId.of(member1, studyGroup))
                .build();

        memberEntity2 = Member.builder()
                .id(MemberId.of(member2, studyGroup))
                .build();

        memberEntity3 = Member.builder()
                .id(MemberId.of(member3, studyGroup))
                .build();

        entityManager.persist(memberEntity1);
        entityManager.flush();
        sleep(25);

        entityManager.persist(memberEntity2);
        entityManager.flush();
        sleep(25);

        entityManager.persist(memberEntity3);
        entityManager.flush();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @DisplayName("스터디 그룹 멤버를 페이징하여 조회할 수 있다")
    void paginateStudyGroupMembers_ShouldReturnPagedResults() {
        // given
        MemberDto.SearchConditions searchConditions = MemberDto.SearchConditions.builder()
                .sort("created_at")
                .order("desc")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<MemberDto.MemberResponse> result = memberQueryRepository.paginateStudyGroupMembers(
                studyGroup, searchConditions, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("생성일 기준 내림차순으로 정렬된 결과를 반환한다")
    void paginateStudyGroupMembers_WithCreatedAtDescSort_ShouldReturnSortedResults() {
        // given
        MemberDto.SearchConditions searchConditions = MemberDto.SearchConditions.builder()
                .sort("created_at")
                .order("desc")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<MemberDto.MemberResponse> result = memberQueryRepository.paginateStudyGroupMembers(
                studyGroup, searchConditions, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);

        // 최신 멤버가 먼저 나와야 함 (charlie -> bob -> alice)
        assertThat(result.getContent().get(0).getUser().getNickname()).isEqualTo("charlie");
        assertThat(result.getContent().get(1).getUser().getNickname()).isEqualTo("bob");
        assertThat(result.getContent().get(2).getUser().getNickname()).isEqualTo("alice");
    }

    @Test
    @DisplayName("생성일 기준 오름차순으로 정렬된 결과를 반환한다")
    void paginateStudyGroupMembers_WithCreatedAtAscSort_ShouldReturnSortedResults() {
        // given
        MemberDto.SearchConditions searchConditions = MemberDto.SearchConditions.builder()
                .sort("created_at")
                .order("asc")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<MemberDto.MemberResponse> result = memberQueryRepository.paginateStudyGroupMembers(
                studyGroup, searchConditions, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);

        // 오래된 멤버가 먼저 나와야 함 (alice -> bob -> charlie)
        assertThat(result.getContent().get(0).getUser().getNickname()).isEqualTo("alice");
        assertThat(result.getContent().get(1).getUser().getNickname()).isEqualTo("bob");
        assertThat(result.getContent().get(2).getUser().getNickname()).isEqualTo("charlie");
    }

    @Test
    @DisplayName("알파벳 순으로 정렬된 결과를 반환한다")
    void paginateStudyGroupMembers_WithAlphabetAscSort_ShouldReturnSortedResults() {
        // given
        MemberDto.SearchConditions searchConditions = MemberDto.SearchConditions.builder()
                .sort("alphabet")
                .order("asc")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<MemberDto.MemberResponse> result = memberQueryRepository.paginateStudyGroupMembers(
                studyGroup, searchConditions, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);

        // 알파벳 순 (alice -> bob -> charlie)
        assertThat(result.getContent().get(0).getUser().getNickname()).isEqualTo("alice");
        assertThat(result.getContent().get(1).getUser().getNickname()).isEqualTo("bob");
        assertThat(result.getContent().get(2).getUser().getNickname()).isEqualTo("charlie");
    }

    @Test
    @DisplayName("알파벳 역순으로 정렬된 결과를 반환한다")
    void paginateStudyGroupMembers_WithAlphabetDescSort_ShouldReturnSortedResults() {
        // given
        MemberDto.SearchConditions searchConditions = MemberDto.SearchConditions.builder()
                .sort("alphabet")
                .order("desc")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<MemberDto.MemberResponse> result = memberQueryRepository.paginateStudyGroupMembers(
                studyGroup, searchConditions, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);

        // 알파벳 역순 (charlie -> bob -> alice)
        assertThat(result.getContent().get(0).getUser().getNickname()).isEqualTo("charlie");
        assertThat(result.getContent().get(1).getUser().getNickname()).isEqualTo("bob");
        assertThat(result.getContent().get(2).getUser().getNickname()).isEqualTo("alice");
    }

    @Test
    @DisplayName("페이징을 통해 멤버를 조회할 수 있다")
    void paginateStudyGroupMembers_WithPaging_ShouldReturnPagedResults() {
        // given
        MemberDto.SearchConditions searchConditions = MemberDto.SearchConditions.builder()
                .sort("created_at")
                .order("desc")
                .build();
        Pageable pageable = PageRequest.of(0, 2); // 첫 번째 페이지, 2개씩

        // when
        Page<MemberDto.MemberResponse> result = memberQueryRepository.paginateStudyGroupMembers(
                studyGroup, searchConditions, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);

        // 첫 번째 페이지: charlie, bob
        assertThat(result.getContent().get(0).getUser().getNickname()).isEqualTo("charlie");
        assertThat(result.getContent().get(1).getUser().getNickname()).isEqualTo("bob");
    }

    @Test
    @DisplayName("두 번째 페이지를 조회할 수 있다")
    void paginateStudyGroupMembers_WithSecondPage_ShouldReturnSecondPageResults() {
        // given
        MemberDto.SearchConditions searchConditions = MemberDto.SearchConditions.builder()
                .sort("created_at")
                .order("desc")
                .build();
        Pageable pageable = PageRequest.of(1, 2); // 두 번째 페이지, 2개씩

        // when
        Page<MemberDto.MemberResponse> result = memberQueryRepository.paginateStudyGroupMembers(
                studyGroup, searchConditions, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);

        // 두 번째 페이지: alice
        assertThat(result.getContent().get(0).getUser().getNickname()).isEqualTo("alice");
    }

    @Test
    @DisplayName("null 검색 조건이 주어져도 기본값으로 처리된다")
    void paginateStudyGroupMembers_WithNullSearchConditions_ShouldUseDefaultValues() {
        // given
        MemberDto.SearchConditions nullConditions = new MemberDto.SearchConditions();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<MemberDto.MemberResponse> result = memberQueryRepository.paginateStudyGroupMembers(
                studyGroup, nullConditions, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);

        // 기본값은 created_at desc이므로 최신 멤버가 먼저 나와야 함
        assertThat(result.getContent().get(0).getUser().getNickname()).isEqualTo("charlie");
        assertThat(result.getContent().get(1).getUser().getNickname()).isEqualTo("bob");
        assertThat(result.getContent().get(2).getUser().getNickname()).isEqualTo("alice");
    }

    @Test
    @DisplayName("멤버 정보에 사용자 정보가 포함되어 있다")
    void paginateStudyGroupMembers_ShouldIncludeUserInformation() {
        // given
        MemberDto.SearchConditions searchConditions = MemberDto.SearchConditions.builder()
                .sort("created_at")
                .order("desc")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<MemberDto.MemberResponse> result = memberQueryRepository.paginateStudyGroupMembers(
                studyGroup, searchConditions, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);

        MemberDto.MemberResponse firstMember = result.getContent().get(0);
        assertThat(firstMember.getUser()).isNotNull();
        assertThat(firstMember.getUser().getId()).isEqualTo(member3.getId());
        assertThat(firstMember.getUser().getEmail()).isEqualTo("member3@example.com");
        assertThat(firstMember.getUser().getNickname()).isEqualTo("charlie");

        assertThat(firstMember.getCreatedAt()).isNotNull();
        assertThat(firstMember.getLastModifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("멤버가 없는 스터디 그룹도 정상적으로 처리된다")
    void paginateStudyGroupMembers_WithNoMembers_ShouldReturnEmptyResults() {
        // given
        // 멤버가 없는 새로운 스터디 그룹 생성
        StudyGroup emptyStudyGroup = StudyGroup.builder()
                .name("빈 스터디 그룹")
                .summary("멤버가 없는 스터디 그룹")
                .maxMembers(10)
                .memberCount(0)
                .currentStep(1)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        entityManager.persist(emptyStudyGroup);
        entityManager.flush();

        MemberDto.SearchConditions searchConditions = MemberDto.SearchConditions.builder()
                .sort("created_at")
                .order("desc")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<MemberDto.MemberResponse> result = memberQueryRepository.paginateStudyGroupMembers(
                emptyStudyGroup, searchConditions, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
    }
}