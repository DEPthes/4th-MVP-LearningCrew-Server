package com.depth.learningcrew.domain.qna.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.depth.learningcrew.domain.qna.dto.QAndADto;
import com.depth.learningcrew.domain.qna.entity.QAndA;
import com.depth.learningcrew.domain.qna.repository.QAndARepository;
import com.depth.learningcrew.domain.studygroup.entity.Member;
import com.depth.learningcrew.domain.studygroup.entity.MemberId;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.MemberRepository;
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
class QAndAServiceIntegrationTest {

  @Autowired
  private QAndAService qAndAService;

  @Autowired
  private QAndARepository qAndARepository;

  @Autowired
  private StudyGroupRepository studyGroupRepository;

  @Autowired
  private MemberRepository memberRepository;

  @PersistenceContext
  private EntityManager entityManager;

  private User owner;
  private User member;
  private User nonMember;
  private UserDetails ownerDetails;
  private UserDetails memberDetails;
  private UserDetails nonMemberDetails;
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

    member = User.builder()
        .email("member@example.com")
        .password("password")
        .nickname("member")
        .birthday(LocalDate.of(1995, 1, 1))
        .gender(Gender.FEMALE)
        .role(Role.USER)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    nonMember = User.builder()
        .email("nonmember@example.com")
        .password("password")
        .nickname("nonmember")
        .birthday(LocalDate.of(1992, 1, 1))
        .gender(Gender.MALE)
        .role(Role.USER)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    // 스터디 그룹 생성
    studyGroup = StudyGroup.builder()
        .name("테스트 스터디 그룹")
        .summary("테스트 스터디 그룹입니다.")
        .content("테스트 스터디 그룹 내용입니다.")
        .maxMembers(10)
        .memberCount(2)
        .currentStep(1)
        .startDate(LocalDate.now())
        .endDate(LocalDate.now().plusMonths(3))
        .owner(owner)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    // 엔티티들을 데이터베이스에 저장
    entityManager.persist(owner);
    entityManager.persist(member);
    entityManager.persist(nonMember);
    entityManager.persist(studyGroup);

    // 스터디 그룹 소유자를 멤버로 추가
    Member ownerMemberEntity = Member.builder()
        .id(MemberId.of(owner, studyGroup))
        .build();
    entityManager.persist(ownerMemberEntity);
    studyGroup.getMembers().add(ownerMemberEntity);

    // 멤버 추가
    Member memberEntity = Member.builder()
        .id(MemberId.of(member, studyGroup))
        .build();
    entityManager.persist(memberEntity);
    studyGroup.getMembers().add(memberEntity);

    entityManager.flush();

    // UserDetails 생성
    ownerDetails = UserDetails.builder()
        .user(owner)
        .build();

    memberDetails = UserDetails.builder()
        .user(member)
        .build();

    nonMemberDetails = UserDetails.builder()
        .user(nonMember)
        .build();
  }

  @Test
  @DisplayName("스터디 그룹 멤버가 질문을 성공적으로 생성할 수 있다")
  void createQAndA_AsMember_ShouldCreateSuccessfully() {
    // given
    QAndADto.QAndACreateRequest request = QAndADto.QAndACreateRequest.builder()
        .title("테스트 질문")
        .content("테스트 질문 내용입니다.")
        .attachedFiles(List.of())
        .attachedImages(List.of())
        .build();

    // when
    QAndADto.QAndAResponse result = qAndAService.createQAndA(request, studyGroup.getId(), 1, memberDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("테스트 질문");
    assertThat(result.getStep()).isEqualTo(1);

    // 데이터베이스에서 확인
    QAndA savedQAndA = qAndARepository.findById(result.getId()).orElse(null);
    assertThat(savedQAndA).isNotNull();
    assertThat(savedQAndA.getTitle()).isEqualTo("테스트 질문");
    assertThat(savedQAndA.getContent()).isEqualTo("테스트 질문 내용입니다.");
    assertThat(savedQAndA.getStudyGroup().getId()).isEqualTo(studyGroup.getId());
  }

  @Test
  @DisplayName("스터디 그룹 소유자가 질문을 성공적으로 생성할 수 있다")
  void createQAndA_AsOwner_ShouldCreateSuccessfully() {
    // given
    QAndADto.QAndACreateRequest request = QAndADto.QAndACreateRequest.builder()
        .title("소유자 질문")
        .content("소유자가 작성한 질문입니다.")
        .attachedFiles(List.of())
        .attachedImages(List.of())
        .build();

    // when
    QAndADto.QAndAResponse result = qAndAService.createQAndA(request, studyGroup.getId(), 1, ownerDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("소유자 질문");
    assertThat(result.getStep()).isEqualTo(1);
  }

  @Test
  @DisplayName("첨부 파일과 이미지가 포함된 질문을 생성할 수 있다")
  void createQAndA_WithAttachedFiles_ShouldCreateSuccessfully() {
    // given
    MockMultipartFile file1 = new MockMultipartFile("file1", "test1.pdf", "application/pdf", "test content".getBytes());
    MockMultipartFile image1 = new MockMultipartFile("image1", "test1.jpg", "image/jpeg", "test image".getBytes());

    QAndADto.QAndACreateRequest request = QAndADto.QAndACreateRequest.builder()
        .title("파일 첨부 질문")
        .content("파일이 첨부된 질문입니다.")
        .attachedFiles(List.of(file1))
        .attachedImages(List.of(image1))
        .build();

    // when
    QAndADto.QAndAResponse result = qAndAService.createQAndA(request, studyGroup.getId(), 1, memberDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("파일 첨부 질문");
    assertThat(result.getAttachedFiles()).isEqualTo(1);
    assertThat(result.getAttachedImages()).isEqualTo(1);
  }

  @Test
  @DisplayName("여러 첨부 파일과 이미지가 포함된 질문을 생성할 수 있다")
  void createQAndA_WithMultipleAttachedFiles_ShouldCreateSuccessfully() {
    // given
    MockMultipartFile file1 = new MockMultipartFile("file1", "test1.pdf", "application/pdf",
        "test content 1".getBytes());
    MockMultipartFile file2 = new MockMultipartFile("file2", "test2.txt", "text/plain", "test content 2".getBytes());
    MockMultipartFile image1 = new MockMultipartFile("image1", "test1.jpg", "image/jpeg", "test image 1".getBytes());
    MockMultipartFile image2 = new MockMultipartFile("image2", "test2.png", "image/png", "test image 2".getBytes());

    QAndADto.QAndACreateRequest request = QAndADto.QAndACreateRequest.builder()
        .title("다중 파일 첨부 질문")
        .content("여러 파일이 첨부된 질문입니다.")
        .attachedFiles(List.of(file1, file2))
        .attachedImages(List.of(image1, image2))
        .build();

    // when
    QAndADto.QAndAResponse result = qAndAService.createQAndA(request, studyGroup.getId(), 1, memberDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("다중 파일 첨부 질문");
    assertThat(result.getAttachedFiles()).isEqualTo(2);
    assertThat(result.getAttachedImages()).isEqualTo(2);
  }

  @Test
  @DisplayName("존재하지 않는 스터디 그룹에 질문을 생성하려고 하면 예외가 발생한다")
  void createQAndA_WithNonExistentStudyGroup_ShouldThrowException() {
    // given
    QAndADto.QAndACreateRequest request = QAndADto.QAndACreateRequest.builder()
        .title("테스트 질문")
        .content("테스트 질문 내용입니다.")
        .attachedFiles(List.of())
        .attachedImages(List.of())
        .build();

    // when & then
    assertThatThrownBy(() -> qAndAService.createQAndA(request, 999L, 1, memberDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_GROUP_NOT_FOUND);
  }

  @Test
  @DisplayName("현재 스텝이 아닌 스텝에 질문을 생성하려고 하면 예외가 발생한다")
  void createQAndA_WithWrongStep_ShouldThrowException() {
    // given
    QAndADto.QAndACreateRequest request = QAndADto.QAndACreateRequest.builder()
        .title("테스트 질문")
        .content("테스트 질문 내용입니다.")
        .attachedFiles(List.of())
        .attachedImages(List.of())
        .build();

    // when & then
    assertThatThrownBy(() -> qAndAService.createQAndA(request, studyGroup.getId(), 2, memberDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_GROUP_NOT_CURRENT_STEP);
  }

  @Test
  @DisplayName("스터디 그룹 멤버가 아닌 사용자가 질문을 생성하려고 하면 예외가 발생한다")
  void createQAndA_WithNonMember_ShouldThrowException() {
    // given
    QAndADto.QAndACreateRequest request = QAndADto.QAndACreateRequest.builder()
        .title("테스트 질문")
        .content("테스트 질문 내용입니다.")
        .attachedFiles(List.of())
        .attachedImages(List.of())
        .build();

    // when & then
    assertThatThrownBy(() -> qAndAService.createQAndA(request, studyGroup.getId(), 1, nonMemberDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_GROUP_NOT_MEMBER);
  }

  @Test
  @DisplayName("빈 첨부 파일 리스트로 질문을 생성할 수 있다")
  void createQAndA_WithEmptyAttachedFiles_ShouldCreateSuccessfully() {
    // given
    QAndADto.QAndACreateRequest request = QAndADto.QAndACreateRequest.builder()
        .title("빈 파일 질문")
        .content("첨부 파일이 없는 질문입니다.")
        .attachedFiles(List.of())
        .attachedImages(List.of())
        .build();

    // when
    QAndADto.QAndAResponse result = qAndAService.createQAndA(request, studyGroup.getId(), 1, memberDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("빈 파일 질문");
    assertThat(result.getAttachedFiles()).isEqualTo(0);
    assertThat(result.getAttachedImages()).isEqualTo(0);
  }

  @Test
  @DisplayName("null 첨부 파일 리스트로 질문을 생성할 수 있다")
  void createQAndA_WithNullAttachedFiles_ShouldCreateSuccessfully() {
    // given
    QAndADto.QAndACreateRequest request = QAndADto.QAndACreateRequest.builder()
        .title("null 파일 질문")
        .content("첨부 파일이 null인 질문입니다.")
        .attachedFiles(null)
        .attachedImages(null)
        .build();

    // when
    QAndADto.QAndAResponse result = qAndAService.createQAndA(request, studyGroup.getId(), 1, memberDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("null 파일 질문");
    assertThat(result.getAttachedFiles()).isEqualTo(0);
    assertThat(result.getAttachedImages()).isEqualTo(0);
  }

  @Test
  @DisplayName("스터디 그룹의 현재 스텝을 변경한 후 해당 스텝에 질문을 생성할 수 있다")
  void createQAndA_AfterStepChange_ShouldCreateSuccessfully() {
    // given
    // 스터디 그룹의 현재 스텝을 2로 변경
    studyGroup.setCurrentStep(2);
    entityManager.merge(studyGroup);
    entityManager.flush();

    QAndADto.QAndACreateRequest request = QAndADto.QAndACreateRequest.builder()
        .title("스텝 2 질문")
        .content("스텝 2에서 작성한 질문입니다.")
        .attachedFiles(List.of())
        .attachedImages(List.of())
        .build();

    // when
    QAndADto.QAndAResponse result = qAndAService.createQAndA(request, studyGroup.getId(), 2, memberDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("스텝 2 질문");
    assertThat(result.getStep()).isEqualTo(2);
  }

  @Test
  @DisplayName("여러 질문을 연속으로 생성할 수 있다")
  void createQAndA_MultipleQuestions_ShouldCreateSuccessfully() {
    // given
    QAndADto.QAndACreateRequest request1 = QAndADto.QAndACreateRequest.builder()
        .title("첫 번째 질문")
        .content("첫 번째 질문 내용입니다.")
        .attachedFiles(List.of())
        .attachedImages(List.of())
        .build();

    QAndADto.QAndACreateRequest request2 = QAndADto.QAndACreateRequest.builder()
        .title("두 번째 질문")
        .content("두 번째 질문 내용입니다.")
        .attachedFiles(List.of())
        .attachedImages(List.of())
        .build();

    // when
    QAndADto.QAndAResponse result1 = qAndAService.createQAndA(request1, studyGroup.getId(), 1, memberDetails);
    QAndADto.QAndAResponse result2 = qAndAService.createQAndA(request2, studyGroup.getId(), 1, memberDetails);

    // then
    assertThat(result1).isNotNull();
    assertThat(result2).isNotNull();
    assertThat(result1.getId()).isNotEqualTo(result2.getId());
    assertThat(result1.getTitle()).isEqualTo("첫 번째 질문");
    assertThat(result2.getTitle()).isEqualTo("두 번째 질문");
  }
}

