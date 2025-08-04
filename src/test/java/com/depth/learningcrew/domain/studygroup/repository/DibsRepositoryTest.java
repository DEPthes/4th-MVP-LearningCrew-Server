package com.depth.learningcrew.domain.studygroup.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.depth.learningcrew.domain.studygroup.entity.Dibs;
import com.depth.learningcrew.domain.studygroup.entity.DibsId;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.user.entity.Gender;
import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.domain.user.entity.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@DataJpaTest
@ActiveProfiles("test")
class DibsRepositoryTest {

  @Autowired
  private DibsRepository dibsRepository;

  @PersistenceContext
  private EntityManager entityManager;

  private User user1;
  private User user2;
  private StudyGroup studyGroup1;
  private StudyGroup studyGroup2;
  private DibsId dibsId1;
  private DibsId dibsId2;

  @BeforeEach
  void setUp() {
    // 테스트 사용자 생성
    user1 = User.builder()
        .email("user1@example.com")
        .password("password")
        .nickname("user1")
        .birthday(LocalDate.of(1990, 1, 1))
        .gender(Gender.MALE)
        .role(Role.USER)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    user2 = User.builder()
        .email("user2@example.com")
        .password("password")
        .nickname("user2")
        .birthday(LocalDate.of(1995, 1, 1))
        .gender(Gender.FEMALE)
        .role(Role.USER)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    // 테스트 스터디 그룹 생성
    studyGroup1 = StudyGroup.builder()
        .name("스터디 그룹 1")
        .summary("스터디 그룹 1입니다.")
        .content("스터디 그룹 1 내용입니다.")
        .maxMembers(10)
        .memberCount(3)
        .currentStep(1)
        .startDate(LocalDate.now())
        .endDate(LocalDate.now().plusMonths(3))
        .owner(user1)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    studyGroup2 = StudyGroup.builder()
        .name("스터디 그룹 2")
        .summary("스터디 그룹 2입니다.")
        .content("스터디 그룹 2 내용입니다.")
        .maxMembers(5)
        .memberCount(2)
        .currentStep(1)
        .startDate(LocalDate.now())
        .endDate(LocalDate.now().plusMonths(2))
        .owner(user2)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    // 엔티티들을 데이터베이스에 저장
    entityManager.persist(user1);
    entityManager.persist(user2);
    entityManager.persist(studyGroup1);
    entityManager.persist(studyGroup2);
    entityManager.flush();

    // DibsId 생성
    dibsId1 = new DibsId(user1, studyGroup1);
    dibsId2 = new DibsId(user2, studyGroup2);
  }

  @Test
  @DisplayName("찜하기를 저장할 수 있다")
  void save_ShouldSaveDibsSuccessfully() {
    // given
    Dibs dibs = Dibs.from(dibsId1);

    // when
    Dibs savedDibs = dibsRepository.save(dibs);

    // then
    assertThat(savedDibs).isNotNull();
    assertThat(savedDibs.getId()).isEqualTo(dibsId1);
    assertThat(dibsRepository.existsById(dibsId1)).isTrue();
  }

  @Test
  @DisplayName("찜하기를 조회할 수 있다")
  void findById_ShouldReturnDibs() {
    // given
    Dibs dibs = Dibs.from(dibsId1);
    dibsRepository.save(dibs);

    // when
    Dibs foundDibs = dibsRepository.findById(dibsId1).orElse(null);

    // then
    assertThat(foundDibs).isNotNull();
    assertThat(foundDibs.getId()).isEqualTo(dibsId1);
    assertThat(foundDibs.getId().getUser()).isEqualTo(user1);
    assertThat(foundDibs.getId().getStudyGroup()).isEqualTo(studyGroup1);
  }

  @Test
  @DisplayName("존재하지 않는 찜하기를 조회하면 null을 반환한다")
  void findById_WithNonExistentId_ShouldReturnNull() {
    // given
    DibsId nonExistentId = new DibsId(user1, studyGroup2);

    // when
    Dibs foundDibs = dibsRepository.findById(nonExistentId).orElse(null);

    // then
    assertThat(foundDibs).isNull();
  }

  @Test
  @DisplayName("찜하기 존재 여부를 확인할 수 있다")
  void existsById_ShouldReturnCorrectBoolean() {
    // given
    Dibs dibs = Dibs.from(dibsId1);
    dibsRepository.save(dibs);

    // when & then
    assertThat(dibsRepository.existsById(dibsId1)).isTrue();
    assertThat(dibsRepository.existsById(dibsId2)).isFalse();
  }

  @Test
  @DisplayName("찜하기를 삭제할 수 있다")
  void deleteById_ShouldDeleteDibsSuccessfully() {
    // given
    Dibs dibs = Dibs.from(dibsId1);
    dibsRepository.save(dibs);
    assertThat(dibsRepository.existsById(dibsId1)).isTrue();

    // when
    dibsRepository.deleteById(dibsId1);

    // then
    assertThat(dibsRepository.existsById(dibsId1)).isFalse();
  }

  @Test
  @DisplayName("여러 찜하기를 저장하고 조회할 수 있다")
  void saveAndFind_MultipleDibs_ShouldWorkCorrectly() {
    // given
    Dibs dibs1 = Dibs.from(dibsId1);
    Dibs dibs2 = Dibs.from(dibsId2);

    // when
    dibsRepository.save(dibs1);
    dibsRepository.save(dibs2);

    // then
    assertThat(dibsRepository.existsById(dibsId1)).isTrue();
    assertThat(dibsRepository.existsById(dibsId2)).isTrue();

    Dibs foundDibs1 = dibsRepository.findById(dibsId1).orElse(null);
    Dibs foundDibs2 = dibsRepository.findById(dibsId2).orElse(null);

    assertThat(foundDibs1).isNotNull();
    assertThat(foundDibs2).isNotNull();
    assertThat(foundDibs1.getId()).isEqualTo(dibsId1);
    assertThat(foundDibs2.getId()).isEqualTo(dibsId2);
  }

  @Test
  @DisplayName("찜하기 엔티티의 생성 시간이 자동으로 설정된다")
  void save_ShouldSetCreatedAtAutomatically() {
    // given
    Dibs dibs = Dibs.from(dibsId1);

    // when
    Dibs savedDibs = dibsRepository.save(dibs);

    // then
    assertThat(savedDibs.getCreatedAt()).isNotNull();
    assertThat(savedDibs.getLastModifiedAt()).isNotNull();
  }

  @Test
  @DisplayName("찜하기 엔티티의 수정 시간이 자동으로 설정된다")
  void save_ShouldSetLastModifiedAtAutomatically() {
    // given
    Dibs dibs = Dibs.from(dibsId1);

    // when
    Dibs savedDibs = dibsRepository.save(dibs);

    // then
    assertThat(savedDibs.getLastModifiedAt()).isNotNull();
  }
}