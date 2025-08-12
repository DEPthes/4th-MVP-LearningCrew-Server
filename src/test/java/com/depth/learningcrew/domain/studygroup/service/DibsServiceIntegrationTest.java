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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class DibsServiceIntegrationTest {

  @Autowired
  private DibsService dibsService;

  @Autowired
  private DibsRepository dibsRepository;

  @Autowired
  private StudyGroupRepository studyGroupRepository;

  @PersistenceContext
  private EntityManager entityManager;

  private User owner;
  private User otherUser;
  private UserDetails ownerDetails;
  private UserDetails otherUserDetails;
  private StudyGroup studyGroup;

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

    otherUser = User.builder()
        .email("other@example.com")
        .password("password")
        .nickname("other")
        .birthday(LocalDate.of(1995, 1, 1))
        .gender(Gender.FEMALE)
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
    entityManager.persist(otherUser);
    entityManager.persist(studyGroup);
    entityManager.flush();

    // UserDetails 객체 생성
    ownerDetails = UserDetails.builder()
        .user(owner)
        .build();

    otherUserDetails = UserDetails.builder()
        .user(otherUser)
        .build();
  }

  @Test
  @DisplayName("찜하지 않은 상태에서 찜하기를 하면 찜하기가 추가되고 true를 반환한다")
  void toggleDibs_WhenNotDibs_ShouldAddDibsAndReturnTrue() {
    // given
    Long groupId = studyGroup.getId();

    // when
    DibsDto.DibsResponse result = dibsService.toggleDibs(groupId, otherUserDetails);

    // then
    assertThat(result.getDibs()).isTrue();

    // 데이터베이스에 실제로 저장되었는지 확인
    DibsId dibsId = new DibsId(otherUser, studyGroup);
    assertThat(dibsRepository.existsById(dibsId)).isTrue();
  }

  @Test
  @DisplayName("이미 찜한 상태에서 찜하기를 하면 찜하기가 삭제되고 false를 반환한다")
  void toggleDibs_WhenAlreadyDibs_ShouldRemoveDibsAndReturnFalse() {
    // given
    Long groupId = studyGroup.getId();

    // 먼저 찜하기 추가
    dibsService.toggleDibs(groupId, otherUserDetails);

    // when - 찜하기 취소
    DibsDto.DibsResponse result = dibsService.toggleDibs(groupId, otherUserDetails);

    // then
    assertThat(result.getDibs()).isFalse();

    // 데이터베이스에서 실제로 삭제되었는지 확인
    DibsId dibsId = new DibsId(otherUser, studyGroup);
    assertThat(dibsRepository.existsById(dibsId)).isFalse();
  }

  @Test
  @DisplayName("존재하지 않는 스터디 그룹에 찜하기를 하면 예외가 발생한다")
  void toggleDibs_WithNonExistentStudyGroup_ShouldThrowException() {
    // given
    Long nonExistentGroupId = 999L;

    // when & then
    assertThatThrownBy(() -> dibsService.toggleDibs(nonExistentGroupId, otherUserDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_GROUP_NOT_FOUND);
  }

  @Test
  @DisplayName("여러 번 찜하기를 토글해도 정상적으로 동작한다")
  void toggleDibs_MultipleToggles_ShouldWorkCorrectly() {
    // given
    Long groupId = studyGroup.getId();

    // when & then
    // 첫 번째 찜하기 (찜하기 추가)
    DibsDto.DibsResponse result1 = dibsService.toggleDibs(groupId, otherUserDetails);
    assertThat(result1.getDibs()).isTrue();

    // 두 번째 찜하기 (찜하기 취소)
    DibsDto.DibsResponse result2 = dibsService.toggleDibs(groupId, otherUserDetails);
    assertThat(result2.getDibs()).isFalse();

    // 세 번째 찜하기 (다시 찜하기 추가)
    DibsDto.DibsResponse result3 = dibsService.toggleDibs(groupId, otherUserDetails);
    assertThat(result3.getDibs()).isTrue();

    // 네 번째 찜하기 (다시 찜하기 취소)
    DibsDto.DibsResponse result4 = dibsService.toggleDibs(groupId, otherUserDetails);
    assertThat(result4.getDibs()).isFalse();
  }

  @Test
  @DisplayName("여러 사용자가 같은 스터디 그룹을 찜할 수 있다")
  void toggleDibs_MultipleUsers_ShouldWorkIndependently() {
    // given
    Long groupId = studyGroup.getId();

    // when & then
    // 첫 번째 사용자 찜하기
    DibsDto.DibsResponse result1 = dibsService.toggleDibs(groupId, otherUserDetails);
    assertThat(result1.getDibs()).isTrue();

    // 두 번째 사용자(owner) 찜하기
    DibsDto.DibsResponse result2 = dibsService.toggleDibs(groupId, ownerDetails);
    assertThat(result2.getDibs()).isTrue();

    // 각각의 찜하기가 독립적으로 존재하는지 확인
    DibsId dibsId1 = new DibsId(otherUser, studyGroup);
    DibsId dibsId2 = new DibsId(owner, studyGroup);
    assertThat(dibsRepository.existsById(dibsId1)).isTrue();
    assertThat(dibsRepository.existsById(dibsId2)).isTrue();

    // 첫 번째 사용자만 찜하기 취소
    DibsDto.DibsResponse result3 = dibsService.toggleDibs(groupId, otherUserDetails);
    assertThat(result3.getDibs()).isFalse();

    // 첫 번째 사용자의 찜하기만 삭제되고, 두 번째 사용자의 찜하기는 유지되는지 확인
    assertThat(dibsRepository.existsById(dibsId1)).isFalse();
    assertThat(dibsRepository.existsById(dibsId2)).isTrue();
  }

  @Test
  @DisplayName("찜하기 엔티티가 올바른 정보로 생성된다")
  void toggleDibs_ShouldCreateCorrectDibsEntity() {
    // given
    Long groupId = studyGroup.getId();

    // when
    dibsService.toggleDibs(groupId, otherUserDetails);

    // then
    DibsId dibsId = new DibsId(otherUser, studyGroup);
    Dibs savedDibs = dibsRepository.findById(dibsId).orElse(null);

    assertThat(savedDibs).isNotNull();
    assertThat(savedDibs.getId().getUser()).isEqualTo(otherUser);
    assertThat(savedDibs.getId().getStudyGroup()).isEqualTo(studyGroup);
  }

  @Test
  @DisplayName("스터디 그룹 소유자도 자신의 그룹을 찜할 수 있다")
  void toggleDibs_OwnerCanDibsOwnGroup_ShouldWorkCorrectly() {
    // given
    Long groupId = studyGroup.getId();

    // when
    DibsDto.DibsResponse result = dibsService.toggleDibs(groupId, ownerDetails);

    // then
    assertThat(result.getDibs()).isTrue();

    // 데이터베이스에 실제로 저장되었는지 확인
    DibsId dibsId = new DibsId(owner, studyGroup);
    assertThat(dibsRepository.existsById(dibsId)).isTrue();
  }
}