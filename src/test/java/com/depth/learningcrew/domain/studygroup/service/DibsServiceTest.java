package com.depth.learningcrew.domain.studygroup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.depth.learningcrew.domain.studygroup.dto.DibsDto;
import com.depth.learningcrew.domain.studygroup.entity.Dibs;
import com.depth.learningcrew.domain.studygroup.entity.DibsId;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.DibsRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.domain.user.entity.Gender;
import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

@ExtendWith(MockitoExtension.class)
class DibsServiceTest {

  @Mock
  private DibsRepository dibsRepository;

  @Mock
  private StudyGroupRepository studyGroupRepository;

  @InjectMocks
  private DibsService dibsService;

  private User testUser;
  private UserDetails testUserDetails;
  private StudyGroup testStudyGroup;
  private DibsId testDibsId;

  @BeforeEach
  void setUp() {
    // 테스트 사용자 설정
    testUser = User.builder()
        .id(1)
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

    testDibsId = new DibsId(testUser, testStudyGroup);
  }

  @Test
  @DisplayName("찜하지 않은 상태에서 찜하기를 하면 찜하기가 추가되고 true를 반환한다")
  void toggleDibs_WhenNotDibs_ShouldAddDibsAndReturnTrue() {
    // given
    Long groupId = 1L;
    when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(testStudyGroup));
    when(dibsRepository.existsById(testDibsId)).thenReturn(false);
    when(dibsRepository.save(any(Dibs.class))).thenReturn(Dibs.from(testDibsId));

    // when
    DibsDto.DibsResponse result = dibsService.toggleDibs(groupId, testUserDetails);

    // then
    assertThat(result.getDibs()).isTrue();
    verify(studyGroupRepository, times(1)).findById(groupId);
    verify(dibsRepository, times(1)).existsById(testDibsId);
    verify(dibsRepository, times(1)).save(any(Dibs.class));
  }

  @Test
  @DisplayName("이미 찜한 상태에서 찜하기를 하면 찜하기가 삭제되고 false를 반환한다")
  void toggleDibs_WhenAlreadyDibs_ShouldRemoveDibsAndReturnFalse() {
    // given
    Long groupId = 1L;
    when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(testStudyGroup));
    when(dibsRepository.existsById(testDibsId)).thenReturn(true);

    // when
    DibsDto.DibsResponse result = dibsService.toggleDibs(groupId, testUserDetails);

    // then
    assertThat(result.getDibs()).isFalse();
    verify(studyGroupRepository, times(1)).findById(groupId);
    verify(dibsRepository, times(1)).existsById(testDibsId);
    verify(dibsRepository, times(1)).deleteById(testDibsId);
  }

  @Test
  @DisplayName("존재하지 않는 스터디 그룹에 찜하기를 하면 예외가 발생한다")
  void toggleDibs_WithNonExistentStudyGroup_ShouldThrowException() {
    // given
    Long groupId = 999L;
    when(studyGroupRepository.findById(groupId)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> dibsService.toggleDibs(groupId, testUserDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_GROUP_NOT_FOUND);

    verify(studyGroupRepository, times(1)).findById(groupId);
    verify(dibsRepository, times(0)).existsById(any());
    verify(dibsRepository, times(0)).save(any());
    verify(dibsRepository, times(0)).deleteById(any());
  }

  @Test
  @DisplayName("찜하기 추가 시 Dibs 엔티티가 올바르게 생성된다")
  void toggleDibs_WhenAddingDibs_ShouldCreateCorrectDibsEntity() {
    // given
    Long groupId = 1L;
    when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(testStudyGroup));
    when(dibsRepository.existsById(testDibsId)).thenReturn(false);
    when(dibsRepository.save(any(Dibs.class))).thenAnswer(invocation -> {
      Dibs dibs = invocation.getArgument(0);
      assertThat(dibs.getId()).isEqualTo(testDibsId);
      return dibs;
    });

    // when
    dibsService.toggleDibs(groupId, testUserDetails);

    // then
    verify(dibsRepository, times(1)).save(any(Dibs.class));
  }

  @Test
  @DisplayName("찜하기 삭제 시 올바른 DibsId로 삭제된다")
  void toggleDibs_WhenRemovingDibs_ShouldDeleteWithCorrectDibsId() {
    // given
    Long groupId = 1L;
    when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(testStudyGroup));
    when(dibsRepository.existsById(testDibsId)).thenReturn(true);

    // when
    dibsService.toggleDibs(groupId, testUserDetails);

    // then
    verify(dibsRepository, times(1)).deleteById(testDibsId);
  }

  @Test
  @DisplayName("다른 사용자와 스터디 그룹으로 찜하기를 할 수 있다")
  void toggleDibs_WithDifferentUserAndGroup_ShouldWorkCorrectly() {
    // given
    User otherUser = User.builder()
        .id(2)
        .email("other@example.com")
        .password("password")
        .nickname("otherUser")
        .birthday(LocalDate.of(1995, 1, 1))
        .gender(Gender.FEMALE)
        .role(Role.USER)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    UserDetails otherUserDetails = UserDetails.builder()
        .user(otherUser)
        .build();

    StudyGroup otherStudyGroup = StudyGroup.builder()
        .id(2L)
        .name("다른 스터디 그룹")
        .summary("다른 스터디 그룹입니다.")
        .maxMembers(5)
        .startDate(LocalDate.now())
        .endDate(LocalDate.now().plusMonths(2))
        .owner(otherUser)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    DibsId otherDibsId = new DibsId(otherUser, otherStudyGroup);
    Long groupId = 2L;

    when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(otherStudyGroup));
    when(dibsRepository.existsById(otherDibsId)).thenReturn(false);
    when(dibsRepository.save(any(Dibs.class))).thenReturn(Dibs.from(otherDibsId));

    // when
    DibsDto.DibsResponse result = dibsService.toggleDibs(groupId, otherUserDetails);

    // then
    assertThat(result.getDibs()).isTrue();
    verify(studyGroupRepository, times(1)).findById(groupId);
    verify(dibsRepository, times(1)).existsById(otherDibsId);
    verify(dibsRepository, times(1)).save(any(Dibs.class));
  }
}