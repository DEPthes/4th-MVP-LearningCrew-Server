package com.depth.learningcrew.domain.qna.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.depth.learningcrew.domain.file.entity.CommentImageFile;
import com.depth.learningcrew.domain.file.entity.HandlingType;
import com.depth.learningcrew.domain.file.entity.QAndAAttachedFile;
import com.depth.learningcrew.domain.file.entity.QAndAImageFile;
import com.depth.learningcrew.domain.file.handler.FileHandler;
import com.depth.learningcrew.domain.qna.dto.QAndADto;
import com.depth.learningcrew.domain.qna.entity.Comment;
import com.depth.learningcrew.domain.qna.entity.QAndA;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.entity.StudyStep;
import com.depth.learningcrew.domain.studygroup.entity.StudyStepId;
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
class QAndAServiceIntegrationTest {

  @Autowired
  private QAndAService qAndAService;

  @MockitoBean
  private FileHandler fileHandler;

  @PersistenceContext
  private EntityManager entityManager;

  private static volatile long testCounter = 0;
  private long currentTestId;

  private User owner;
  private User questionAuthor;
  private User otherUser;
  private UserDetails ownerDetails;
  private UserDetails questionAuthorDetails;
  private UserDetails otherUserDetails;
  private StudyGroup studyGroup;
  private StudyStep studyStep;
  private QAndA qAndA;

  @BeforeEach
  void setUp() {
    // 각 테스트마다 고유한 ID 생성
    currentTestId = ++testCounter;

    // 기존 데이터 완전 정리 - 외래키 제약조건을 고려한 순서
    entityManager.createQuery("DELETE FROM QAndA q").executeUpdate();
    entityManager.createQuery("DELETE FROM StudyStep s").executeUpdate();
    entityManager.createQuery("DELETE FROM StudyGroup g").executeUpdate();
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

    questionAuthor = User.builder()
        .email("author_" + testId + "@test.com")
        .password("password")
        .nickname("author_" + testId)
        .birthday(LocalDate.of(1991, 1, 1))
        .gender(Gender.FEMALE)
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
        .maxMembers(10)
        .memberCount(3)
        .currentStep(1)
        .startDate(LocalDate.now())
        .endDate(LocalDate.now().plusMonths(3))
        .owner(owner)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    // 스터디 스텝 생성
    studyStep = StudyStep.builder()
        .id(StudyStepId.of(1, studyGroup))
        .endDate(LocalDate.now().plusDays(30))
        .build();

    // 엔티티들을 데이터베이스에 저장
    entityManager.persist(owner);
    entityManager.persist(questionAuthor);
    entityManager.persist(otherUser);
    entityManager.persist(studyGroup);
    entityManager.persist(studyStep);
    entityManager.flush();

    // Q&A 생성
    qAndA = QAndA.builder()
        .title("원본 질문 제목_" + testId)
        .content("원본 질문 내용_" + testId)
        .step(1)
        .studyGroup(studyGroup)
        .createdBy(questionAuthor)
        .lastModifiedBy(questionAuthor)
        .createdAt(LocalDateTime.now().minusDays(1))
        .lastModifiedAt(LocalDateTime.now().minusDays(1))
        .build();

    entityManager.persist(qAndA);
    entityManager.flush();

    // UserDetails 생성
    ownerDetails = UserDetails.builder()
        .user(owner)
        .build();

    questionAuthorDetails = UserDetails.builder()
        .user(questionAuthor)
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
  @DisplayName("질문 작성자가 자신의 질문을 수정할 수 있다")
  void updateQAndA_ByAuthor_Success() {
    // given
    Long studyGroupId = studyGroup.getId();
    Long qnaId = qAndA.getId();
    String newTitle = "수정된 질문 제목";
    String newContent = "수정된 질문 내용";

    QAndADto.QAndAUpdateRequest request = QAndADto.QAndAUpdateRequest.builder()
        .title(newTitle)
        .content(newContent)
        .build();

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(
        qnaId, request, questionAuthorDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(qnaId);
    assertThat(result.getTitle()).isEqualTo(newTitle);
    assertThat(result.getContent()).isEqualTo(newContent);
    assertThat(result.getCreatedBy().getId()).isEqualTo(questionAuthor.getId());
    // lastModifiedBy는 JPA Auditing으로 자동 설정되므로 테스트에서 검증하지 않음

    // 데이터베이스에서 실제 변경 확인
    QAndA updatedQAndA = entityManager.find(QAndA.class, qnaId);
    assertThat(updatedQAndA.getTitle()).isEqualTo(newTitle);
    assertThat(updatedQAndA.getContent()).isEqualTo(newContent);
  }

  @Test
  @DisplayName("스터디 그룹 주최자가 질문을 수정할 수 있다")
  void updateQAndA_ByOwner_Success() {
    // given
    Long studyGroupId = studyGroup.getId();
    Long qnaId = qAndA.getId();
    String newTitle = "주최자가 수정한 질문 제목";
    String newContent = "주최자가 수정한 질문 내용";

    QAndADto.QAndAUpdateRequest request = QAndADto.QAndAUpdateRequest.builder()
        .title(newTitle)
        .content(newContent)
        .build();

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(
        qnaId, request, ownerDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(qnaId);
    assertThat(result.getTitle()).isEqualTo(newTitle);
    assertThat(result.getContent()).isEqualTo(newContent);
    assertThat(result.getCreatedBy().getId()).isEqualTo(questionAuthor.getId());
    // lastModifiedBy는 JPA Auditing으로 자동 설정되므로 테스트에서 검증하지 않음

    // 데이터베이스에서 실제 변경 확인
    QAndA updatedQAndA = entityManager.find(QAndA.class, qnaId);
    assertThat(updatedQAndA.getTitle()).isEqualTo(newTitle);
    assertThat(updatedQAndA.getContent()).isEqualTo(newContent);
  }

  @Test
  @DisplayName("권한이 없는 사용자가 질문을 수정하려고 하면 예외가 발생한다")
  void updateQAndA_ByUnauthorizedUser_ThrowsException() {
    // given
    Long studyGroupId = studyGroup.getId();
    Long qnaId = qAndA.getId();

    QAndADto.QAndAUpdateRequest request = QAndADto.QAndAUpdateRequest.builder()
        .title("수정된 제목")
        .content("수정된 내용")
        .build();

    // when & then
    assertThatThrownBy(() -> qAndAService.updateQAndA(
        qnaId, request, otherUserDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QANDA_NOT_AUTHORIZED);

    // 데이터베이스에서 변경되지 않았는지 확인
    QAndA unchangedQAndA = entityManager.find(QAndA.class, qnaId);
    assertThat(unchangedQAndA.getTitle()).isEqualTo("원본 질문 제목_" + currentTestId);
    assertThat(unchangedQAndA.getContent()).isEqualTo("원본 질문 내용_" + currentTestId);
  }

  @Test
  @DisplayName("존재하지 않는 질문을 수정하려고 하면 예외가 발생한다")
  void updateQAndA_NonExistentQAndA_ThrowsException() {
    // given
    Long studyGroupId = studyGroup.getId();
    Long nonExistentQnaId = 999L;

    QAndADto.QAndAUpdateRequest request = QAndADto.QAndAUpdateRequest.builder()
        .title("수정된 제목")
        .content("수정된 내용")
        .build();

    // when & then
    assertThatThrownBy(() -> qAndAService.updateQAndA(
        nonExistentQnaId, request, questionAuthorDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QANDA_NOT_FOUND);
  }

  @Test
  @DisplayName("제목만 수정할 수 있다")
  void updateQAndA_OnlyTitle_Success() {
    // given
    Long studyGroupId = studyGroup.getId();
    Long qnaId = qAndA.getId();
    String newTitle = "제목만 수정된 질문";

    QAndADto.QAndAUpdateRequest request = QAndADto.QAndAUpdateRequest.builder()
        .title(newTitle)
        .build();

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(
        qnaId, request, questionAuthorDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo(newTitle);
    assertThat(result.getContent()).isEqualTo("원본 질문 내용_" + currentTestId); // 원본 내용 유지

    // 데이터베이스에서 실제 변경 확인
    QAndA updatedQAndA = entityManager.find(QAndA.class, qnaId);
    assertThat(updatedQAndA.getTitle()).isEqualTo(newTitle);
    assertThat(updatedQAndA.getContent()).isEqualTo("원본 질문 내용_" + currentTestId);
  }

  @Test
  @DisplayName("내용만 수정할 수 있다")
  void updateQAndA_OnlyContent_Success() {
    // given
    Long studyGroupId = studyGroup.getId();
    Long qnaId = qAndA.getId();
    String newContent = "내용만 수정된 질문";

    QAndADto.QAndAUpdateRequest request = QAndADto.QAndAUpdateRequest.builder()
        .content(newContent)
        .build();

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(
        qnaId, request, questionAuthorDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("원본 질문 제목_" + currentTestId); // 원본 제목 유지
    assertThat(result.getContent()).isEqualTo(newContent);

    // 데이터베이스에서 실제 변경 확인
    QAndA updatedQAndA = entityManager.find(QAndA.class, qnaId);
    assertThat(updatedQAndA.getTitle()).isEqualTo("원본 질문 제목_" + currentTestId);
    assertThat(updatedQAndA.getContent()).isEqualTo(newContent);
  }

  @Test
  @DisplayName("null 값이 전달되어도 기존 값이 유지된다")
  void updateQAndA_WithNullValues_KeepsOriginalValues() {
    // given
    Long studyGroupId = studyGroup.getId();
    Long qnaId = qAndA.getId();

    QAndADto.QAndAUpdateRequest request = QAndADto.QAndAUpdateRequest.builder()
        .title(null)
        .content(null)
        .build();

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(
        qnaId, request, questionAuthorDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("원본 질문 제목_" + currentTestId);
    assertThat(result.getContent()).isEqualTo("원본 질문 내용_" + currentTestId);

    // 데이터베이스에서 실제 변경 확인
    QAndA updatedQAndA = entityManager.find(QAndA.class, qnaId);
    assertThat(updatedQAndA.getTitle()).isEqualTo("원본 질문 제목_" + currentTestId);
    assertThat(updatedQAndA.getContent()).isEqualTo("원본 질문 내용_" + currentTestId);
  }

  @Test
  @DisplayName("빈 문자열로 수정할 수 있다")
  void updateQAndA_WithEmptyStrings_Success() {
    // given
    Long studyGroupId = studyGroup.getId();
    Long qnaId = qAndA.getId();

    QAndADto.QAndAUpdateRequest request = QAndADto.QAndAUpdateRequest.builder()
        .title("")
        .content("")
        .build();

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(
        qnaId, request, questionAuthorDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEmpty();
    assertThat(result.getContent()).isEmpty();

    // 데이터베이스에서 실제 변경 확인
    QAndA updatedQAndA = entityManager.find(QAndA.class, qnaId);
    assertThat(updatedQAndA.getTitle()).isEmpty();
    assertThat(updatedQAndA.getContent()).isEmpty();
  }

  @Test
  @DisplayName("새로운 첨부 파일을 추가할 수 있다")
  void updateQAndA_WithNewAttachedFiles_Success() {
    // given
    Long studyGroupId = studyGroup.getId();
    Long qnaId = qAndA.getId();

    MockMultipartFile file1 = new MockMultipartFile(
        "newAttachedFiles",
        "test1.txt",
        "text/plain",
        "첨부 파일 내용 1".getBytes());

    MockMultipartFile file2 = new MockMultipartFile(
        "newAttachedFiles",
        "test2.pdf",
        "application/pdf",
        "첨부 파일 내용 2".getBytes());

    QAndADto.QAndAUpdateRequest request = QAndADto.QAndAUpdateRequest.builder()
        .title("첨부 파일이 추가된 질문")
        .newAttachedFiles(List.of(file1, file2))
        .build();

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(
        qnaId, request, questionAuthorDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getAttachedFiles()).hasSize(2);
    assertThat(result.getAttachedFiles().get(0).getFileName()).isEqualTo("test1.txt");
    assertThat(result.getAttachedFiles().get(1).getFileName()).isEqualTo("test2.pdf");
  }

  @Test
  @DisplayName("새로운 첨부 이미지를 추가할 수 있다")
  void updateQAndA_WithNewAttachedImages_Success() {
    // given
    Long studyGroupId = studyGroup.getId();
    Long qnaId = qAndA.getId();

    MockMultipartFile image1 = new MockMultipartFile(
        "newAttachedImages",
        "image1.jpg",
        "image/jpeg",
        "이미지 데이터 1".getBytes());

    MockMultipartFile image2 = new MockMultipartFile(
        "newAttachedImages",
        "image2.png",
        "image/png",
        "이미지 데이터 2".getBytes());

    QAndADto.QAndAUpdateRequest request = QAndADto.QAndAUpdateRequest.builder()
        .title("첨부 이미지가 추가된 질문")
        .newAttachedImages(List.of(image1, image2))
        .build();

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(
        qnaId, request, questionAuthorDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getAttachedImages()).hasSize(2);
    assertThat(result.getAttachedImages().get(0).getFileName()).isEqualTo("image1.jpg");
    assertThat(result.getAttachedImages().get(1).getFileName()).isEqualTo("image2.png");
  }

  @Test
  @DisplayName("기존 첨부 파일을 삭제할 수 있다")
  void updateQAndA_DeleteAttachedFiles_Success() {
    // given
    Long studyGroupId = studyGroup.getId();
    Long qnaId = qAndA.getId();

    // 기존 첨부 파일 생성
    QAndAAttachedFile existingFile1 = QAndAAttachedFile.builder()
        .uuid("file-uuid-1")
        .fileName("existing1.txt")
        .size(100L)
        .handlingType(HandlingType.DOWNLOADABLE)
        .build();
    existingFile1.setQAndA(qAndA);
    qAndA.addAttachedFile(existingFile1);

    QAndAAttachedFile existingFile2 = QAndAAttachedFile.builder()
        .uuid("file-uuid-2")
        .fileName("existing2.pdf")
        .size(200L)
        .handlingType(HandlingType.DOWNLOADABLE)
        .build();
    existingFile2.setQAndA(qAndA);
    qAndA.addAttachedFile(existingFile2);

    entityManager.persist(existingFile1);
    entityManager.persist(existingFile2);
    entityManager.flush();

    QAndADto.QAndAUpdateRequest request = QAndADto.QAndAUpdateRequest.builder()
        .title("첨부 파일이 삭제된 질문")
        .deletedAttachedFiles(List.of("file-uuid-1"))
        .build();

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(
        qnaId, request, questionAuthorDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getAttachedFiles()).hasSize(1);
    assertThat(result.getAttachedFiles().get(0).getFileName()).isEqualTo("existing2.pdf");
  }

  @Test
  @DisplayName("기존 첨부 이미지를 삭제할 수 있다")
  void updateQAndA_DeleteAttachedImages_Success() {
    // given
    Long studyGroupId = studyGroup.getId();
    Long qnaId = qAndA.getId();

    // 기존 첨부 이미지 생성
    QAndAImageFile existingImage1 = QAndAImageFile.builder()
        .uuid("image-uuid-1")
        .fileName("existing1.jpg")
        .size(300L)
        .handlingType(HandlingType.IMAGE)
        .build();
    existingImage1.setQAndA(qAndA);
    qAndA.addAttachedImage(existingImage1);

    QAndAImageFile existingImage2 = QAndAImageFile.builder()
        .uuid("image-uuid-2")
        .fileName("existing2.png")
        .size(400L)
        .handlingType(HandlingType.IMAGE)
        .build();
    existingImage2.setQAndA(qAndA);
    qAndA.addAttachedImage(existingImage2);

    entityManager.persist(existingImage1);
    entityManager.persist(existingImage2);
    entityManager.flush();

    QAndADto.QAndAUpdateRequest request = QAndADto.QAndAUpdateRequest.builder()
        .title("첨부 이미지가 삭제된 질문")
        .deletedAttachedImages(List.of("image-uuid-1"))
        .build();

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(
        qnaId, request, questionAuthorDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getAttachedImages()).hasSize(1);
    assertThat(result.getAttachedImages().get(0).getFileName()).isEqualTo("existing2.png");
  }

  @Test
  @DisplayName("존재하지 않는 파일 ID로 삭제를 시도해도 예외가 발생하지 않는다")
  void updateQAndA_DeleteNonExistentFiles_NoException() {
    // given
    Long studyGroupId = studyGroup.getId();
    Long qnaId = qAndA.getId();

    QAndADto.QAndAUpdateRequest request = QAndADto.QAndAUpdateRequest.builder()
        .title("존재하지 않는 파일 삭제 시도")
        .deletedAttachedFiles(List.of("non-existent-file-id"))
        .deletedAttachedImages(List.of("non-existent-image-id"))
        .build();

    // when & then - 예외가 발생하지 않아야 함
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(
        qnaId, request, questionAuthorDetails);

    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("존재하지 않는 파일 삭제 시도");
  }

  @Test
  @DisplayName("복합적인 수정 작업을 수행할 수 있다")
  void updateQAndA_ComplexUpdate_Success() {
    // given
    Long studyGroupId = studyGroup.getId();
    Long qnaId = qAndA.getId();

    // 기존 첨부 파일 생성
    QAndAAttachedFile existingFile = QAndAAttachedFile.builder()
        .uuid("existing-file-uuid")
        .fileName("existing.txt")
        .size(100L)
        .handlingType(HandlingType.DOWNLOADABLE)
        .build();
    existingFile.setQAndA(qAndA);
    qAndA.addAttachedFile(existingFile);

    entityManager.persist(existingFile);
    entityManager.flush();

    // 새로운 파일과 이미지
    MockMultipartFile newFile = new MockMultipartFile(
        "newAttachedFiles",
        "new.txt",
        "text/plain",
        "새 파일 내용".getBytes());

    MockMultipartFile newImage = new MockMultipartFile(
        "newAttachedImages",
        "new.jpg",
        "image/jpeg",
        "새 이미지 데이터".getBytes());

    QAndADto.QAndAUpdateRequest request = QAndADto.QAndAUpdateRequest.builder()
        .title("복합 수정된 질문")
        .content("복합 수정된 내용")
        .newAttachedFiles(List.of(newFile))
        .newAttachedImages(List.of(newImage))
        .deletedAttachedFiles(List.of("existing-file-uuid"))
        .build();

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(
        qnaId, request, questionAuthorDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("복합 수정된 질문");
    assertThat(result.getContent()).isEqualTo("복합 수정된 내용");
    assertThat(result.getAttachedFiles()).hasSize(1);
    assertThat(result.getAttachedFiles().get(0).getFileName()).isEqualTo("new.txt");
    assertThat(result.getAttachedImages()).hasSize(1);
    assertThat(result.getAttachedImages().get(0).getFileName()).isEqualTo("new.jpg");
  }

  @Test
  @DisplayName("수정 후 lastModifiedAt이 업데이트된다")
  void updateQAndA_LastModifiedAtUpdated() {
    // given
    Long studyGroupId = studyGroup.getId();
    Long qnaId = qAndA.getId();
    LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);

    QAndADto.QAndAUpdateRequest request = QAndADto.QAndAUpdateRequest.builder()
        .title("수정된 제목")
        .build();

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(
        qnaId, request, questionAuthorDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getLastModifiedAt()).isNotNull();

    // 데이터베이스에서 실제 변경 확인
    QAndA updatedQAndA = entityManager.find(QAndA.class, qnaId);
    assertThat(updatedQAndA.getLastModifiedAt()).isAfter(beforeUpdate);
  }

  @Test
  @DisplayName("수정 후 lastModifiedBy가 설정된다")
  void updateQAndA_LastModifiedByUpdated() {
    // given
    Long studyGroupId = studyGroup.getId();
    Long qnaId = qAndA.getId();

    QAndADto.QAndAUpdateRequest request = QAndADto.QAndAUpdateRequest.builder()
        .title("수정된 제목")
        .build();

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(
        qnaId, request, questionAuthorDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getLastModifiedBy()).isNotNull();
    // lastModifiedBy는 JPA Auditing으로 자동 설정되므로 구체적인 값은 검증하지 않음

    // 데이터베이스에서 실제 변경 확인
    QAndA updatedQAndA = entityManager.find(QAndA.class, qnaId);
    assertThat(updatedQAndA.getLastModifiedBy()).isNotNull();
  }

  @Test
  @DisplayName("질문 작성자가 자신의 질문을 삭제할 수 있다 (첨부 및 댓글 이미지 정리 포함)")
  void deleteQAndA_ByAuthor_Success_WithFileCleanup() {
    // given
    Long qnaId = qAndA.getId();

    // Q&A 첨부 파일/이미지 추가
    QAndAAttachedFile attachedFile = QAndAAttachedFile.builder()
        .uuid("qna-file-uuid")
        .fileName("file.txt")
        .size(123L)
        .handlingType(HandlingType.DOWNLOADABLE)
        .build();
    attachedFile.setQAndA(qAndA);
    qAndA.addAttachedFile(attachedFile);

    QAndAImageFile attachedImage = QAndAImageFile.builder()
        .uuid("qna-image-uuid")
        .fileName("image.jpg")
        .size(456L)
        .handlingType(HandlingType.IMAGE)
        .build();
    attachedImage.setQAndA(qAndA);
    qAndA.addAttachedImage(attachedImage);

    // 댓글 + 댓글 이미지 추가
    Comment comment = Comment.builder()
        .content("댓글")
        .qAndA(qAndA)
        .build();
    qAndA.addComment(comment);

    CommentImageFile commentImage = CommentImageFile.builder()
        .uuid("comment-image-uuid")
        .fileName("cimg.png")
        .size(789L)
        .handlingType(HandlingType.IMAGE)
        .comment(comment)
        .build();
    comment.addAttachedImage(commentImage);

    entityManager.persist(attachedFile);
    entityManager.persist(attachedImage);
    entityManager.persist(comment);
    entityManager.persist(commentImage);
    entityManager.flush();

    // when
    qAndAService.deleteQAndA(qnaId, questionAuthorDetails);

    // then
    assertThat(entityManager.find(QAndA.class, qnaId)).isNull();
    verify(fileHandler, times(3)).deleteFile(any()); // Q&A 파일 1, Q&A 이미지 1, 댓글 이미지 1
  }

  @Test
  @DisplayName("스터디 그룹 주최자가 질문을 삭제할 수 있다")
  void deleteQAndA_ByOwner_Success() {
    // given
    Long qnaId = qAndA.getId();

    // when
    qAndAService.deleteQAndA(qnaId, ownerDetails);

    // then
    assertThat(entityManager.find(QAndA.class, qnaId)).isNull();
  }

  @Test
  @DisplayName("권한이 없는 사용자가 질문 삭제 시 예외 발생")
  void deleteQAndA_ByUnauthorized_Throws() {
    // given
    Long qnaId = qAndA.getId();

    // when & then
    assertThatThrownBy(() -> qAndAService.deleteQAndA(qnaId, otherUserDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QANDA_NOT_AUTHORIZED);

    assertThat(entityManager.find(QAndA.class, qnaId)).isNotNull();
  }

  @Test
  @DisplayName("존재하지 않는 질문 삭제 시 예외 발생")
  void deleteQAndA_NonExistent_Throws() {
    // when & then
    assertThatThrownBy(() -> qAndAService.deleteQAndA(9999L, questionAuthorDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QANDA_NOT_FOUND);
  }

  @Test
  @DisplayName("관리자는 질문을 삭제할 수 있다")
  void deleteQAndA_ByAdmin_Success() {
    // given
    Long qnaId = qAndA.getId();
    User admin = User.builder()
        .email("admin@t.com")
        .password("p")
        .nickname("admin")
        .birthday(LocalDate.of(1980, 1, 1))
        .gender(Gender.MALE)
        .role(Role.ADMIN)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();
    UserDetails adminDetails = UserDetails.builder().user(admin).build();

    // when
    qAndAService.deleteQAndA(qnaId, adminDetails);

    // then
    assertThat(entityManager.find(QAndA.class, qnaId)).isNull();
  }
}
