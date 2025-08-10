package com.depth.learningcrew.domain.qna.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import com.depth.learningcrew.domain.file.entity.CommentAttachedFile;
import com.depth.learningcrew.domain.file.entity.CommentImageFile;
import com.depth.learningcrew.domain.file.entity.HandlingType;
import com.depth.learningcrew.domain.file.handler.FileHandler;
import com.depth.learningcrew.domain.qna.dto.CommentDto;
import com.depth.learningcrew.domain.qna.entity.Comment;
import com.depth.learningcrew.domain.qna.entity.QAndA;
import com.depth.learningcrew.domain.studygroup.entity.Member;
import com.depth.learningcrew.domain.studygroup.entity.MemberId;
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
class CommentServiceIntegrationTest {

  @Autowired
  private CommentService commentService;

  @PersistenceContext
  private EntityManager entityManager;

  @MockitoBean
  private FileHandler fileHandler; // 실제 파일 저장을 막기 위한 목킹

  private static volatile long testCounter = 0;
  private long currentTestId;

  private User owner;
  private User member;
  private User other;
  private User admin;
  private UserDetails ownerDetails;
  private UserDetails memberDetails;
  private UserDetails otherDetails;
  private UserDetails adminDetails;
  private StudyGroup studyGroup;
  private StudyStep studyStep;
  private QAndA qna;
  private Comment comment;

  @BeforeEach
  void setUp() {
    currentTestId = ++testCounter;

    entityManager.createQuery("DELETE FROM Comment c").executeUpdate();
    entityManager.createQuery("DELETE FROM QAndA q").executeUpdate();
    entityManager.createQuery("DELETE FROM StudyStep s").executeUpdate();
    entityManager.createQuery("DELETE FROM StudyGroup g").executeUpdate();
    entityManager.createQuery("DELETE FROM User u").executeUpdate();
    entityManager.flush();
    entityManager.clear();

    owner = User.builder()
        .email("owner_" + currentTestId + "@t.com")
        .password("p")
        .nickname("owner_" + currentTestId)
        .birthday(LocalDate.of(1990, 1, 1))
        .gender(Gender.MALE)
        .role(Role.USER)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    member = User.builder()
        .email("member_" + currentTestId + "@t.com")
        .password("p")
        .nickname("member_" + currentTestId)
        .birthday(LocalDate.of(1992, 2, 2))
        .gender(Gender.FEMALE)
        .role(Role.USER)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    other = User.builder()
        .email("other_" + currentTestId + "@t.com")
        .password("p")
        .nickname("other_" + currentTestId)
        .birthday(LocalDate.of(1995, 5, 5))
        .gender(Gender.MALE)
        .role(Role.USER)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    admin = User.builder()
        .email("admin_" + currentTestId + "@t.com")
        .password("p")
        .nickname("admin_" + currentTestId)
        .birthday(LocalDate.of(1980, 3, 3))
        .gender(Gender.MALE)
        .role(Role.ADMIN)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    entityManager.persist(owner);
    entityManager.persist(member);
    entityManager.persist(other);
    entityManager.persist(admin);

    studyGroup = StudyGroup.builder()
        .name("sg_" + currentTestId)
        .summary("sum")
        .content("content")
        .maxMembers(10)
        .memberCount(2)
        .currentStep(1)
        .startDate(LocalDate.now())
        .endDate(LocalDate.now().plusMonths(1))
        .owner(owner)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();
    entityManager.persist(studyGroup);

    studyStep = StudyStep.builder()
        .id(StudyStepId.of(1, studyGroup))
        .endDate(LocalDate.now().plusDays(30))
        .build();
    entityManager.persist(studyStep);

    qna = QAndA.builder()
        .title("t_" + currentTestId)
        .content("c_" + currentTestId)
        .step(1)
        .studyGroup(studyGroup)
        .createdBy(member)
        .lastModifiedBy(member)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();
    entityManager.persist(qna);

    // 댓글 생성(작성자: member)
    comment = Comment.builder()
        .content("original")
        .qAndA(qna)
        .createdBy(member)
        .lastModifiedBy(member)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();
    entityManager.persist(comment);
    // 멤버십: member, admin을 스터디 그룹 멤버로 추가
    Member memberMembership = Member.builder().id(MemberId.of(member, studyGroup)).build();
    Member adminMembership = Member.builder().id(MemberId.of(admin, studyGroup)).build();
    entityManager.persist(memberMembership);
    entityManager.persist(adminMembership);
    entityManager.flush();

    ownerDetails = UserDetails.builder().user(owner).build();
    memberDetails = UserDetails.builder().user(member).build();
    otherDetails = UserDetails.builder().user(other).build();
    adminDetails = UserDetails.builder().user(admin).build();
  }

  @AfterEach
  void tearDown() {
    if (entityManager != null)
      entityManager.clear();
  }

  @Test
  @DisplayName("댓글 작성자가 자신의 댓글을 수정할 수 있다")
  void updateComment_ByAuthor_Success() {
    // when
    var result = commentService.updateComment(comment.getId(),
        CommentDto.CommentUpdateRequest.builder().content("updated").build(), memberDetails);

    // then
    assertThat(result.getContent()).isEqualTo("updated");
  }

  @Test
  @DisplayName("관리자는 댓글을 수정할 수 있다")
  void updateComment_ByAdmin_Success() {
    // when
    var result = commentService.updateComment(comment.getId(),
        CommentDto.CommentUpdateRequest.builder().content("admin").build(), adminDetails);

    // then
    assertThat(result.getContent()).isEqualTo("admin");
  }

  @Test
  @DisplayName("권한 없는 사용자는 댓글 수정 불가")
  void updateComment_Unauthorized_Throws() {
    // when & then
    assertThatThrownBy(() -> commentService.updateComment(comment.getId(),
        CommentDto.CommentUpdateRequest.builder().content("x").build(), otherDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);
  }

  @Test
  @DisplayName("새 파일/이미지 추가 및 기존 파일/이미지 삭제를 동시에 수행")
  void updateComment_ComplexUpdate_Success() {
    // given 기존 파일/이미지 달기
    CommentAttachedFile existingFile = CommentAttachedFile.builder()
        .uuid("ef-" + currentTestId)
        .fileName("ef.txt")
        .handlingType(HandlingType.DOWNLOADABLE)
        .size(10L)
        .comment(comment)
        .build();
    comment.addAttachedFile(existingFile);

    CommentImageFile existingImage = CommentImageFile.builder()
        .uuid("ei-" + currentTestId)
        .fileName("ei.jpg")
        .handlingType(HandlingType.IMAGE)
        .size(11L)
        .comment(comment)
        .build();
    comment.addAttachedImage(existingImage);

    entityManager.persist(existingFile);
    entityManager.persist(existingImage);
    entityManager.flush();

    MockMultipartFile newFile = new MockMultipartFile("newAttachedFiles", "nf.txt", "text/plain",
        "a".getBytes());
    MockMultipartFile newImage = new MockMultipartFile("newAttachedImages", "ni.jpg", "image/jpeg",
        "b".getBytes());

    CommentDto.CommentUpdateRequest req = CommentDto.CommentUpdateRequest.builder()
        .content("complex")
        .newAttachedFiles(List.of(newFile))
        .newAttachedImages(List.of(newImage))
        .deletedAttachedFiles(List.of(existingFile.getUuid()))
        .deletedAttachedImages(List.of(existingImage.getUuid()))
        .build();

    // when
    var result = commentService.updateComment(comment.getId(), req, memberDetails);

    // then
    assertThat(result.getContent()).isEqualTo("complex");
    verify(fileHandler, times(1)).deleteFile(existingFile);
    verify(fileHandler, times(1)).deleteFile(existingImage);
    verify(fileHandler, times(1)).saveFile(org.mockito.ArgumentMatchers.eq(newFile),
        org.mockito.ArgumentMatchers.any());
    verify(fileHandler, times(1)).saveFile(org.mockito.ArgumentMatchers.eq(newImage),
        org.mockito.ArgumentMatchers.any());
  }

  @Test
  @DisplayName("답변 작성자가 자신의 답변을 삭제할 수 있다")
  void deleteComment_ByAuthor_Success() {
    // when
    commentService.deleteComment(comment.getId(), memberDetails);

    // then
    assertThat(entityManager.find(Comment.class, comment.getId())).isNull();
  }

  @Test
  @DisplayName("스터디 그룹 주최자가 답변을 삭제할 수 있다")
  void deleteComment_ByOwner_Success() {
    // when
    commentService.deleteComment(comment.getId(), ownerDetails);

    // then
    assertThat(entityManager.find(Comment.class, comment.getId())).isNull();
  }

  @Test
  @DisplayName("관리자가 답변을 삭제할 수 있다")
  void deleteComment_ByAdmin_Success() {
    // when
    commentService.deleteComment(comment.getId(), adminDetails);

    // then
    assertThat(entityManager.find(Comment.class, comment.getId())).isNull();
  }

  @Test
  @DisplayName("답변 작성자도 아니고 그룹 주최자도 아닌 사용자가 삭제 요청시 403 에러 발생")
  void deleteComment_NotAuthorAndNotOwner_Forbidden() {
    // when & then
    assertThatThrownBy(
        () -> commentService.deleteComment(comment.getId(), otherDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);
  }

  @Test
  @DisplayName("존재하지 않는 스터디 그룹이면 삭제 시도시 예외 발생")
  void deleteComment_StudyGroupNotFound() {
    // when & then
    assertThatThrownBy(() -> commentService.deleteComment(999L, memberDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
  }

  @Test
  @DisplayName("존재하지 않는 Q&A면 삭제 시도시 예외 발생")
  void deleteComment_QAndANotFound() {
    // when & then
    assertThatThrownBy(() -> commentService.deleteComment(999L, memberDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
  }

  @Test
  @DisplayName("존재하지 않는 답변이면 삭제 시도시 예외 발생")
  void deleteComment_CommentNotFound() {
    // when & then
    assertThatThrownBy(() -> commentService.deleteComment(999L, memberDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
  }

  @Test
  @DisplayName("답변 삭제시 첨부 파일들도 함께 삭제된다")
  void deleteComment_WithAttachments_DeleteFiles() {
    // given 첨부 파일 추가
    CommentAttachedFile attachedFile = CommentAttachedFile.builder()
        .uuid("af-" + currentTestId)
        .fileName("af.txt")
        .handlingType(HandlingType.DOWNLOADABLE)
        .size(10L)
        .comment(comment)
        .build();
    comment.addAttachedFile(attachedFile);

    CommentImageFile imageFile = CommentImageFile.builder()
        .uuid("ai-" + currentTestId)
        .fileName("ai.jpg")
        .handlingType(HandlingType.IMAGE)
        .size(11L)
        .comment(comment)
        .build();
    comment.addAttachedImage(imageFile);

    entityManager.persist(attachedFile);
    entityManager.persist(imageFile);
    entityManager.flush();

    // when
    commentService.deleteComment(comment.getId(), memberDetails);

    // then
    verify(fileHandler).deleteFile(attachedFile);
    verify(fileHandler).deleteFile(imageFile);
    assertThat(entityManager.find(Comment.class, comment.getId())).isNull();
  }
}
