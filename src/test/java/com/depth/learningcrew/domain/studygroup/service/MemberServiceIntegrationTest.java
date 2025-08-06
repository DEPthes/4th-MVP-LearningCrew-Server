package com.depth.learningcrew.domain.studygroup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.depth.learningcrew.domain.studygroup.dto.MemberDto;
import com.depth.learningcrew.domain.studygroup.entity.Member;
import com.depth.learningcrew.domain.studygroup.entity.MemberId;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.user.entity.Gender;
import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MemberServiceIntegrationTest {

    @Autowired
    private MemberService memberService;

    @PersistenceContext
    private EntityManager entityManager;

    private static volatile long testCounter = 0;
    private long currentTestId;

    private User owner;
    private User member1;
    private User member2;
    private User otherUser;
    private UserDetails ownerDetails;
    private UserDetails otherUserDetails;
    private StudyGroup studyGroup;
    private Member memberEntity1;
    private Member memberEntity2;

    @BeforeEach
    void setUp() {
        // 각 테스트마다 고유한 ID 생성
        currentTestId = ++testCounter;

        // 기존 데이터 완전 정리 - 외래키 제약조건을 고려한 순서
        entityManager.createQuery("DELETE FROM Member m").executeUpdate();
        entityManager.createQuery("DELETE FROM Application a").executeUpdate();
        entityManager.createQuery("DELETE FROM Dibs d").executeUpdate();
        entityManager.createQuery("DELETE FROM StudyGroup s").executeUpdate();
        entityManager.createQuery("DELETE FROM User u").executeUpdate();
        entityManager.flush();
        entityManager.clear(); // 영속성 컨텍스트 완전 초기화

        // 테스트 사용자 생성 - 고유한 값 사용
        String testId = String.valueOf(currentTestId);

        owner = User.builder()
                .email("owner_" + testId + "@test.com")
                .password("password")
                .nickname("owner_" + testId)
                .birthday(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        member1 = User.builder()
                .email("member1_" + testId + "@test.com")
                .password("password")
                .nickname("member1_" + testId)
                .birthday(LocalDate.of(1991, 1, 1))
                .gender(Gender.FEMALE)
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        member2 = User.builder()
                .email("member2_" + testId + "@test.com")
                .password("password")
                .nickname("member2_" + testId)
                .birthday(LocalDate.of(1992, 1, 1))
                .gender(Gender.MALE)
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        otherUser = User.builder()
                .email("other_" + testId + "@test.com")
                .password("password")
                .nickname("other_" + testId)
                .birthday(LocalDate.of(1995, 1, 1))
                .gender(Gender.FEMALE)
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        // 스터디 그룹 생성
        studyGroup = StudyGroup.builder()
                .name("테스트 스터디 그룹_" + testId)
                .summary("테스트 스터디 그룹입니다.")
                .content("테스트 스터디 그룹 내용입니다.")
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
        entityManager.persist(otherUser);
        entityManager.persist(studyGroup);
        entityManager.flush();

        // 멤버 엔티티 생성 및 저장
        memberEntity1 = Member.builder()
                .id(MemberId.of(member1, studyGroup))
                .createdAt(LocalDateTime.now().minusDays(1))
                .lastModifiedAt(LocalDateTime.now().minusDays(1))
                .build();

        memberEntity2 = Member.builder()
                .id(MemberId.of(member2, studyGroup))
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        entityManager.persist(memberEntity1);
        entityManager.persist(memberEntity2);
        entityManager.flush();

        // UserDetails 생성
        ownerDetails = UserDetails.builder()
                .user(owner)
                .build();

        otherUserDetails = UserDetails.builder()
                .user(otherUser)
                .build();
    }

    @AfterEach
    void tearDown() {
        // 영속성 컨텍스트 초기화
        if (entityManager != null) {
            entityManager.clear();
        }
    }

    @Test
    @DisplayName("스터디 그룹 멤버를 성공적으로 페이징하여 조회할 수 있다")
    void paginateStudyGroupMembers_ShouldReturnPagedResults() {
        // given
        MemberDto.SearchConditions searchConditions = MemberDto.SearchConditions.builder()
                .sort("created_at")
                .order("desc")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        PagedModel<MemberDto.MemberResponse> result = memberService.paginateStudyGroupMembers(
                studyGroup.getId(), searchConditions, ownerDetails, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getUser().getNickname()).isEqualTo("member2_" + currentTestId); // 최신 멤버
        assertThat(result.getContent().get(1).getUser().getNickname()).isEqualTo("member1_" + currentTestId); // 이전 멤버
    }

    @Test
    @DisplayName("존재하지 않는 스터디 그룹을 조회하려고 하면 예외가 발생한다")
    void paginateStudyGroupMembers_WithNonExistentGroup_ShouldThrowException() {
        // given
        MemberDto.SearchConditions searchConditions = MemberDto.SearchConditions.builder()
                .sort("created_at")
                .order("desc")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when & then
        assertThatThrownBy(() -> memberService.paginateStudyGroupMembers(
                999L, searchConditions, ownerDetails, pageable))
                .isInstanceOf(RestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GLOBAL_NOT_FOUND);
    }

    @Test
    @DisplayName("owner가 아닌 사용자가 멤버를 조회하려고 하면 예외가 발생한다")
    void paginateStudyGroupMembers_WithNonOwner_ShouldThrowException() {
        // given
        MemberDto.SearchConditions searchConditions = MemberDto.SearchConditions.builder()
                .sort("created_at")
                .order("desc")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when & then
        assertThatThrownBy(() -> memberService.paginateStudyGroupMembers(
                studyGroup.getId(), searchConditions, otherUserDetails, pageable))
                .isInstanceOf(RestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);
    }

    @Test
    @DisplayName("알파벳 순으로 멤버를 정렬하여 조회할 수 있다")
    void paginateStudyGroupMembers_WithAlphabetSort_ShouldReturnSortedResults() {
        // given
        MemberDto.SearchConditions searchConditions = MemberDto.SearchConditions.builder()
                .sort("alphabet")
                .order("asc")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        PagedModel<MemberDto.MemberResponse> result = memberService.paginateStudyGroupMembers(
                studyGroup.getId(), searchConditions, ownerDetails, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getUser().getNickname()).isEqualTo("member1_" + currentTestId); // 알파벳 순
        assertThat(result.getContent().get(1).getUser().getNickname()).isEqualTo("member2_" + currentTestId);
    }

    @Test
    @DisplayName("알파벳 역순으로 멤버를 정렬하여 조회할 수 있다")
    void paginateStudyGroupMembers_WithAlphabetDescSort_ShouldReturnSortedResults() {
        // given
        MemberDto.SearchConditions searchConditions = MemberDto.SearchConditions.builder()
                .sort("alphabet")
                .order("desc")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        PagedModel<MemberDto.MemberResponse> result = memberService.paginateStudyGroupMembers(
                studyGroup.getId(), searchConditions, ownerDetails, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getUser().getNickname()).isEqualTo("member2_" + currentTestId); // 알파벳 역순
        assertThat(result.getContent().get(1).getUser().getNickname()).isEqualTo("member1_" + currentTestId);
    }

    @Test
    @DisplayName("페이징을 통해 멤버를 조회할 수 있다")
    void paginateStudyGroupMembers_WithPaging_ShouldReturnPagedResults() {
        // given
        MemberDto.SearchConditions searchConditions = MemberDto.SearchConditions.builder()
                .sort("created_at")
                .order("desc")
                .build();
        Pageable pageable = PageRequest.of(0, 1); // 첫 번째 페이지, 1개씩

        // when
        PagedModel<MemberDto.MemberResponse> result = memberService.paginateStudyGroupMembers(
                studyGroup.getId(), searchConditions, ownerDetails, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUser().getNickname()).isEqualTo("member2_" + currentTestId); // 최신 멤버
    }

    @Test
    @DisplayName("두 번째 페이지를 조회할 수 있다")
    void paginateStudyGroupMembers_WithSecondPage_ShouldReturnSecondPageResults() {
        // given
        MemberDto.SearchConditions searchConditions = MemberDto.SearchConditions.builder()
                .sort("created_at")
                .order("desc")
                .build();
        Pageable pageable = PageRequest.of(1, 1); // 두 번째 페이지, 1개씩

        // when
        PagedModel<MemberDto.MemberResponse> result = memberService.paginateStudyGroupMembers(
                studyGroup.getId(), searchConditions, ownerDetails, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUser().getNickname()).isEqualTo("member1_" + currentTestId); // 이전 멤버
    }

    @Test
    @DisplayName("null 검색 조건이 주어져도 기본값으로 처리된다")
    void paginateStudyGroupMembers_WithNullSearchConditions_ShouldUseDefaultValues() {
        // given
        MemberDto.SearchConditions nullConditions = new MemberDto.SearchConditions();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        PagedModel<MemberDto.MemberResponse> result = memberService.paginateStudyGroupMembers(
                studyGroup.getId(), nullConditions, ownerDetails, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        // 기본값은 created_at desc이므로 최신 멤버가 먼저 나와야 함
        assertThat(result.getContent().get(0).getUser().getNickname()).isEqualTo("member2_" + currentTestId);
        assertThat(result.getContent().get(1).getUser().getNickname()).isEqualTo("member1_" + currentTestId);
    }

    @Test
    @DisplayName("멤버가 없는 스터디 그룹도 정상적으로 처리된다")
    void paginateStudyGroupMembers_WithNoMembers_ShouldReturnEmptyResults() {
        // given
        // 멤버가 없는 새로운 스터디 그룹 생성
        StudyGroup emptyStudyGroup = StudyGroup.builder()
                .name("빈 스터디 그룹_" + currentTestId)
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
        PagedModel<MemberDto.MemberResponse> result = memberService.paginateStudyGroupMembers(
                emptyStudyGroup.getId(), searchConditions, ownerDetails, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
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
        PagedModel<MemberDto.MemberResponse> result = memberService.paginateStudyGroupMembers(
                studyGroup.getId(), searchConditions, ownerDetails, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);

        MemberDto.MemberResponse firstMember = result.getContent().get(0);
        assertThat(firstMember.getUser()).isNotNull();
        assertThat(firstMember.getUser().getId()).isEqualTo(member2.getId());
        assertThat(firstMember.getUser().getEmail()).isEqualTo("member2_" + currentTestId + "@test.com");
        assertThat(firstMember.getUser().getNickname()).isEqualTo("member2_" + currentTestId);

        assertThat(firstMember.getCreatedAt()).isNotNull();
        assertThat(firstMember.getLastModifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("스터디 그룹 멤버 추방 - 성공")
    void expelMember_Success() {
        // given
        Long groupId = studyGroup.getId();
        Long userId = member1.getId();

        // when
        memberService.expelMember(groupId, userId, ownerDetails);

        // then
        // 멤버가 실제로 삭제되었는지 확인
        Member deletedMember = entityManager.find(Member.class, MemberId.of(member1, studyGroup));
        assertThat(deletedMember).isNull();

        // 스터디 그룹의 멤버 수가 감소했는지 확인
        StudyGroup updatedStudyGroup = entityManager.find(StudyGroup.class, groupId);
        assertThat(updatedStudyGroup.getMemberCount()).isEqualTo(2); // 3 -> 2
    }

    @Test
    @DisplayName("스터디 그룹 멤버 추방 - owner가 아닌 경우 실패")
    void expelMember_NotOwner_ThrowsException() {
        // given
        Long groupId = studyGroup.getId();
        Long userId = member1.getId();

        // when & then
        assertThatThrownBy(() -> memberService.expelMember(groupId, userId, otherUserDetails))
                .isInstanceOf(RestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);

        // 멤버가 삭제되지 않았는지 확인
        Member member = entityManager.find(Member.class, MemberId.of(member1, studyGroup));
        assertThat(member).isNotNull();
    }

    @Test
    @DisplayName("스터디 그룹 멤버 추방 - owner 자신을 추방하려는 경우 실패")
    void expelMember_ExpelOwner_ThrowsException() {
        // given
        Long groupId = studyGroup.getId();
        Long userId = owner.getId();

        // when & then
        assertThatThrownBy(() -> memberService.expelMember(groupId, userId, ownerDetails))
                .isInstanceOf(RestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_GROUP_OWNER_CANNOT_BE_EXPELLED);
    }

    @Test
    @DisplayName("스터디 그룹 멤버 추방 - 존재하지 않는 스터디 그룹 실패")
    void expelMember_NonExistentStudyGroup_ThrowsException() {
        // given
        Long groupId = 999L;
        Long userId = member1.getId();

        // when & then
        assertThatThrownBy(() -> memberService.expelMember(groupId, userId, ownerDetails))
                .isInstanceOf(RestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GLOBAL_NOT_FOUND);
    }

    @Test
    @DisplayName("스터디 그룹 멤버 추방 - 존재하지 않는 사용자 실패")
    void expelMember_NonExistentUser_ThrowsException() {
        // given
        Long groupId = studyGroup.getId();
        Long userId = 999L;

        // when & then
        assertThatThrownBy(() -> memberService.expelMember(groupId, userId, ownerDetails))
                .isInstanceOf(RestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GLOBAL_NOT_FOUND);
    }

    @Test
    @DisplayName("스터디 그룹 멤버 추방 - 그룹에 속하지 않은 사용자 실패")
    void expelMember_UserNotInGroup_ThrowsException() {
        // given
        Long groupId = studyGroup.getId();
        Long userId = otherUser.getId();

        // when & then
        assertThatThrownBy(() -> memberService.expelMember(groupId, userId, ownerDetails))
                .isInstanceOf(RestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GLOBAL_NOT_FOUND);
    }

    @Test
    @DisplayName("여러 멤버를 순차적으로 추방할 수 있다")
    void expelMultipleMembers_Success() {
        // given
        Long groupId = studyGroup.getId();

        // when - 첫 번째 멤버 추방
        memberService.expelMember(groupId, member1.getId(), ownerDetails);

        // then - 첫 번째 멤버 추방 확인
        Member deletedMember1 = entityManager.find(Member.class, MemberId.of(member1, studyGroup));
        assertThat(deletedMember1).isNull();

        StudyGroup studyGroupAfterFirstExpel = entityManager.find(StudyGroup.class, groupId);
        assertThat(studyGroupAfterFirstExpel.getMemberCount()).isEqualTo(2);

        // when - 두 번째 멤버 추방
        memberService.expelMember(groupId, member2.getId(), ownerDetails);

        // then - 두 번째 멤버 추방 확인
        Member deletedMember2 = entityManager.find(Member.class, MemberId.of(member2, studyGroup));
        assertThat(deletedMember2).isNull();

        StudyGroup studyGroupAfterSecondExpel = entityManager.find(StudyGroup.class, groupId);
        assertThat(studyGroupAfterSecondExpel.getMemberCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("멤버 추방 후 멤버 조회에서 제외된다")
    void expelMember_ShouldBeExcludedFromMemberList() {
        // given
        Long groupId = studyGroup.getId();
        Long userId = member1.getId();

        // when - 멤버 추방
        memberService.expelMember(groupId, userId, ownerDetails);

        // then - 멤버 조회에서 제외되었는지 확인
        MemberDto.SearchConditions searchConditions = MemberDto.SearchConditions.builder()
                .sort("created_at")
                .order("desc")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        PagedModel<MemberDto.MemberResponse> result = memberService.paginateStudyGroupMembers(
                groupId, searchConditions, ownerDetails, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUser().getId()).isEqualTo(member2.getId());
    }
}