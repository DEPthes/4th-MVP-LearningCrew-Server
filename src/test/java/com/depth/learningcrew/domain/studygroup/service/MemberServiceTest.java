package com.depth.learningcrew.domain.studygroup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
import org.springframework.data.web.PagedModel;

import com.depth.learningcrew.domain.studygroup.dto.MemberDto;
import com.depth.learningcrew.domain.studygroup.entity.Member;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.MemberQueryRepository;
import com.depth.learningcrew.domain.studygroup.repository.MemberRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.domain.user.entity.Gender;
import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.domain.user.repository.UserRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberQueryRepository memberQueryRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MemberService memberService;

    private User testUser;
    private UserDetails testUserDetails;
    private StudyGroup testStudyGroup;
    private MemberDto.SearchConditions searchConditions;
    private Pageable pageable;
    private MemberDto.MemberResponse testMemberResponse;

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

        // 테스트 멤버 응답 DTO 설정
        testMemberResponse = MemberDto.MemberResponse.builder()
                .user(com.depth.learningcrew.domain.user.dto.UserDto.UserResponse.builder()
                        .id(testUser.getId())
                        .email(testUser.getEmail())
                        .nickname(testUser.getNickname())
                        .build())
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        // 검색 조건 설정
        searchConditions = MemberDto.SearchConditions.builder()
                .sort("created_at")
                .order("desc")
                .build();

        // 페이징 설정
        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("스터디 그룹 멤버를 페이징하여 조회할 수 있다")
    void paginateStudyGroupMembers_ShouldReturnPagedResults() {
        // given
        Page<MemberDto.MemberResponse> mockPage = new PageImpl<>(
                List.of(testMemberResponse), pageable, 1L);

        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(testStudyGroup));
        when(memberQueryRepository.paginateStudyGroupMembers(
                eq(testStudyGroup), eq(searchConditions), eq(pageable)))
                .thenReturn(mockPage);

        // when
        PagedModel<MemberDto.MemberResponse> result = memberService.paginateStudyGroupMembers(
                1L, searchConditions, testUserDetails, pageable);

        // then
        verify(studyGroupRepository, times(1)).findById(1L);
        verify(memberQueryRepository, times(1))
                .paginateStudyGroupMembers(eq(testStudyGroup), eq(searchConditions), eq(pageable));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUser().getId()).isEqualTo(testUser.getId());
        assertThat(result.getContent().get(0).getUser().getNickname()).isEqualTo(testUser.getNickname());
    }

    @Test
    @DisplayName("존재하지 않는 스터디 그룹을 조회하려고 하면 예외가 발생한다")
    void paginateStudyGroupMembers_WithNonExistentGroup_ShouldThrowException() {
        // given
        when(studyGroupRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.paginateStudyGroupMembers(
                999L, searchConditions, testUserDetails, pageable))
                .isInstanceOf(RestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GLOBAL_NOT_FOUND);
    }

    @Test
    @DisplayName("owner가 아닌 사용자가 멤버를 조회하려고 하면 예외가 발생한다")
    void paginateStudyGroupMembers_WithNonOwner_ShouldThrowException() {
        // given
        User otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .nickname("otherUser")
                .build();

        UserDetails otherUserDetails = UserDetails.builder()
                .user(otherUser)
                .build();

        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(testStudyGroup));

        // when & then
        assertThatThrownBy(() -> memberService.paginateStudyGroupMembers(
                1L, searchConditions, otherUserDetails, pageable))
                .isInstanceOf(RestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);
    }

    @Test
    @DisplayName("빈 검색 결과가 있을 때도 정상적으로 PagedModel을 반환한다")
    void paginateStudyGroupMembers_WithEmptyResults_ShouldReturnEmptyPagedModel() {
        // given
        Page<MemberDto.MemberResponse> emptyPage = new PageImpl<>(
                List.of(), pageable, 0L);

        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(testStudyGroup));
        when(memberQueryRepository.paginateStudyGroupMembers(
                eq(testStudyGroup), eq(searchConditions), eq(pageable)))
                .thenReturn(emptyPage);

        // when
        PagedModel<MemberDto.MemberResponse> result = memberService.paginateStudyGroupMembers(
                1L, searchConditions, testUserDetails, pageable);

        // then
        verify(studyGroupRepository, times(1)).findById(1L);
        verify(memberQueryRepository, times(1))
                .paginateStudyGroupMembers(eq(testStudyGroup), eq(searchConditions), eq(pageable));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("다양한 정렬 조건으로 멤버를 조회할 수 있다")
    void paginateStudyGroupMembers_WithDifferentSortConditions_ShouldWorkCorrectly() {
        // given
        MemberDto.SearchConditions alphabetSortConditions = MemberDto.SearchConditions.builder()
                .sort("alphabet")
                .order("asc")
                .build();

        Page<MemberDto.MemberResponse> mockPage = new PageImpl<>(
                List.of(testMemberResponse), pageable, 1L);

        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(testStudyGroup));
        when(memberQueryRepository.paginateStudyGroupMembers(
                eq(testStudyGroup), eq(alphabetSortConditions), eq(pageable)))
                .thenReturn(mockPage);

        // when
        PagedModel<MemberDto.MemberResponse> result = memberService.paginateStudyGroupMembers(
                1L, alphabetSortConditions, testUserDetails, pageable);

        // then
        verify(studyGroupRepository, times(1)).findById(1L);
        verify(memberQueryRepository, times(1))
                .paginateStudyGroupMembers(eq(testStudyGroup), eq(alphabetSortConditions), eq(pageable));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("다양한 페이징 설정으로 멤버를 조회할 수 있다")
    void paginateStudyGroupMembers_WithDifferentPageable_ShouldWorkCorrectly() {
        // given
        Pageable customPageable = PageRequest.of(1, 5); // 두 번째 페이지, 5개씩

        Page<MemberDto.MemberResponse> mockPage = new PageImpl<>(
                List.of(testMemberResponse), customPageable, 1L);

        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(testStudyGroup));
        when(memberQueryRepository.paginateStudyGroupMembers(
                eq(testStudyGroup), eq(searchConditions), eq(customPageable)))
                .thenReturn(mockPage);

        // when
        PagedModel<MemberDto.MemberResponse> result = memberService.paginateStudyGroupMembers(
                1L, searchConditions, testUserDetails, customPageable);

        // then
        verify(studyGroupRepository, times(1)).findById(1L);
        verify(memberQueryRepository, times(1))
                .paginateStudyGroupMembers(eq(testStudyGroup), eq(searchConditions), eq(customPageable));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("여러 명의 멤버가 있을 때 모든 결과를 반환한다")
    void paginateStudyGroupMembers_WithMultipleResults_ShouldReturnAllResults() {
        // given
        User secondUser = User.builder()
                .id(2L)
                .email("second@example.com")
                .nickname("secondUser")
                .build();

        MemberDto.MemberResponse secondMemberResponse = MemberDto.MemberResponse.builder()
                .user(com.depth.learningcrew.domain.user.dto.UserDto.UserResponse.builder()
                        .id(secondUser.getId())
                        .email(secondUser.getEmail())
                        .nickname(secondUser.getNickname())
                        .build())
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        Page<MemberDto.MemberResponse> mockPage = new PageImpl<>(
                List.of(testMemberResponse, secondMemberResponse), pageable, 2L);

        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(testStudyGroup));
        when(memberQueryRepository.paginateStudyGroupMembers(
                eq(testStudyGroup), eq(searchConditions), eq(pageable)))
                .thenReturn(mockPage);

        // when
        PagedModel<MemberDto.MemberResponse> result = memberService.paginateStudyGroupMembers(
                1L, searchConditions, testUserDetails, pageable);

        // then
        verify(studyGroupRepository, times(1)).findById(1L);
        verify(memberQueryRepository, times(1))
                .paginateStudyGroupMembers(eq(testStudyGroup), eq(searchConditions), eq(pageable));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getUser().getId()).isEqualTo(1L);
        assertThat(result.getContent().get(1).getUser().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("null 검색 조건이 주어져도 기본값으로 처리된다")
    void paginateStudyGroupMembers_WithNullSearchConditions_ShouldUseDefaultValues() {
        // given
        MemberDto.SearchConditions nullConditions = new MemberDto.SearchConditions();

        Page<MemberDto.MemberResponse> mockPage = new PageImpl<>(
                List.of(testMemberResponse), pageable, 1L);

        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(testStudyGroup));
        when(memberQueryRepository.paginateStudyGroupMembers(
                eq(testStudyGroup), eq(nullConditions), eq(pageable)))
                .thenReturn(mockPage);

        // when
        PagedModel<MemberDto.MemberResponse> result = memberService.paginateStudyGroupMembers(
                1L, nullConditions, testUserDetails, pageable);

        // then
        verify(studyGroupRepository, times(1)).findById(1L);
        verify(memberQueryRepository, times(1))
                .paginateStudyGroupMembers(eq(testStudyGroup), eq(nullConditions), eq(pageable));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("스터디 그룹 멤버 추방 - 성공")
    void expelMember_Success() {
        // given
        Long groupId = 1L;
        Long userId = 2L;
        UserDetails userDetails = mock(UserDetails.class);
        User owner = mock(User.class);
        User userToExpel = mock(User.class);
        StudyGroup studyGroup = mock(StudyGroup.class);
        Member member = mock(Member.class);

        when(userDetails.getUser()).thenReturn(owner);
        when(owner.getId()).thenReturn(1L);
        when(userToExpel.getId()).thenReturn(2L);
        when(studyGroup.getOwner()).thenReturn(owner);
        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToExpel));
        when(memberRepository.findById_UserAndId_StudyGroup(userToExpel, studyGroup))
                .thenReturn(Optional.of(member));

        // when
        memberService.expelMember(groupId, userId, userDetails);

        // then
        verify(memberRepository).delete(member);
        verify(studyGroup).decreaseMemberCount();
    }

    @Test
    @DisplayName("스터디 그룹 멤버 추방 - owner가 아닌 경우 실패")
    void expelMember_NotOwner_ThrowsException() {
        // given
        Long groupId = 1L;
        Long userId = 2L;
        UserDetails userDetails = mock(UserDetails.class);
        User nonOwner = mock(User.class);
        User userToExpel = mock(User.class);
        StudyGroup studyGroup = mock(StudyGroup.class);
        User owner = mock(User.class);

        when(userDetails.getUser()).thenReturn(nonOwner);
        when(nonOwner.getId()).thenReturn(3L);
        when(owner.getId()).thenReturn(1L);
        when(studyGroup.getOwner()).thenReturn(owner);
        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToExpel));

        // when & then
        assertThatThrownBy(() -> memberService.expelMember(groupId, userId, userDetails))
                .isInstanceOf(RestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);
    }

    @Test
    @DisplayName("스터디 그룹 멤버 추방 - owner 자신을 추방하려는 경우 실패")
    void expelMember_ExpelOwner_ThrowsException() {
        // given
        Long groupId = 1L;
        Long userId = 1L; // owner의 ID
        UserDetails userDetails = mock(UserDetails.class);
        User owner = mock(User.class);
        StudyGroup studyGroup = mock(StudyGroup.class);

        when(userDetails.getUser()).thenReturn(owner);
        when(owner.getId()).thenReturn(1L);
        when(studyGroup.getOwner()).thenReturn(owner);
        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));

        // when & then
        assertThatThrownBy(() -> memberService.expelMember(groupId, userId, userDetails))
                .isInstanceOf(RestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_GROUP_OWNER_CANNOT_BE_EXPELLED);
    }

    @Test
    @DisplayName("스터디 그룹 멤버 추방 - 스터디 그룹이 존재하지 않는 경우 실패")
    void expelMember_StudyGroupNotFound_ThrowsException() {
        // given
        Long groupId = 999L;
        Long userId = 2L;
        UserDetails userDetails = mock(UserDetails.class);

        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.expelMember(groupId, userId, userDetails))
                .isInstanceOf(RestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GLOBAL_NOT_FOUND);
    }

    @Test
    @DisplayName("스터디 그룹 멤버 추방 - 멤버가 존재하지 않는 경우 실패")
    void expelMember_MemberNotFound_ThrowsException() {
        // given
        Long groupId = 1L;
        Long userId = 2L;
        UserDetails userDetails = mock(UserDetails.class);
        User owner = mock(User.class);
        User userToExpel = mock(User.class);
        StudyGroup studyGroup = mock(StudyGroup.class);

        when(userDetails.getUser()).thenReturn(owner);
        when(owner.getId()).thenReturn(1L);
        when(userToExpel.getId()).thenReturn(2L);
        when(studyGroup.getOwner()).thenReturn(owner);
        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToExpel));
        when(memberRepository.findById_UserAndId_StudyGroup(userToExpel, studyGroup))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.expelMember(groupId, userId, userDetails))
                .isInstanceOf(RestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GLOBAL_NOT_FOUND);
    }
}