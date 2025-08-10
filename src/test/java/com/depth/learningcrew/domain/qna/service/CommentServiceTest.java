package com.depth.learningcrew.domain.qna.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import org.springframework.mock.web.MockMultipartFile;

import com.depth.learningcrew.domain.file.entity.CommentAttachedFile;
import com.depth.learningcrew.domain.file.entity.CommentImageFile;
import com.depth.learningcrew.domain.file.handler.FileHandler;
import com.depth.learningcrew.domain.qna.dto.CommentDto;
import com.depth.learningcrew.domain.qna.entity.Comment;
import com.depth.learningcrew.domain.qna.entity.QAndA;
import com.depth.learningcrew.domain.qna.repository.CommentRepository;
import com.depth.learningcrew.domain.qna.repository.QAndARepository;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.MemberQueryRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.domain.user.entity.Gender;
import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private QAndARepository qAndARepository;

  @Mock
  private StudyGroupRepository studyGroupRepository;

  @Mock
  private MemberQueryRepository memberQueryRepository;

  @Mock
  private FileHandler fileHandler;

  @InjectMocks
  private CommentService commentService;

  private User author;
  private User anotherUser;
  private User adminUser;
  private UserDetails authorDetails;
  private UserDetails anotherDetails;
  private UserDetails adminDetails;
  private StudyGroup studyGroup;
  private QAndA qAndA;
  private Comment comment;

  @BeforeEach
  void setUp() {
    author = User.builder()
        .id(1L)
        .email("author@test.com")
        .password("p")
        .nickname("author")
        .birthday(LocalDate.of(1990, 1, 1))
        .gender(Gender.MALE)
        .role(Role.USER)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    anotherUser = User.builder()
        .id(2L)
        .email("other@test.com")
        .password("p")
        .nickname("other")
        .birthday(LocalDate.of(1992, 2, 2))
        .gender(Gender.FEMALE)
        .role(Role.USER)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    adminUser = User.builder()
        .id(3L)
        .email("admin@test.com")
        .password("p")
        .nickname("admin")
        .birthday(LocalDate.of(1985, 3, 3))
        .gender(Gender.MALE)
        .role(Role.ADMIN)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    authorDetails = UserDetails.builder().user(author).build();
    anotherDetails = UserDetails.builder().user(anotherUser).build();
    adminDetails = UserDetails.builder().user(adminUser).build();

    studyGroup = StudyGroup.builder()
        .id(10L)
        .name("sg")
        .summary("sum")
        .maxMembers(10)
        .currentStep(1)
        .startDate(LocalDate.now())
        .endDate(LocalDate.now().plusDays(1))
        .owner(author)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    qAndA = QAndA.builder()
        .id(100L)
        .title("Test Q&A")
        .content("Test content")
        .studyGroup(studyGroup)
        .createdBy(author)
        .lastModifiedBy(author)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    comment = Comment.builder()
        .id(100L)
        .content("original content")
        .qAndA(qAndA)
        .attachedFiles(new java.util.ArrayList<>())
        .attachedImages(new java.util.ArrayList<>())
        .createdBy(author)
        .lastModifiedBy(author)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();
  }

  @Test
  @DisplayName("작성자가 댓글 내용을 수정할 수 있다")
  void updateComment_ByAuthor_UpdateContent() {
    // given
    when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(studyGroup));
    when(memberQueryRepository.isMember(studyGroup, author)).thenReturn(true);
    when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

    CommentDto.CommentUpdateRequest request = CommentDto.CommentUpdateRequest.builder()
        .content("updated")
        .build();

    // when
    var result = commentService.updateComment(10L, 100L, request, authorDetails);

    // then
    assertThat(result.getContent()).isEqualTo("updated");
    verify(fileHandler, never()).saveFile(any(), any());
    verify(fileHandler, never()).deleteFile(any());
  }

  @Test
  @DisplayName("새 첨부파일/이미지를 추가할 수 있다")
  void updateComment_AddNewFilesAndImages() {
    // given
    when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(studyGroup));
    when(memberQueryRepository.isMember(studyGroup, author)).thenReturn(true);
    when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

    MockMultipartFile file = new MockMultipartFile("newAttachedFiles", "f.txt", "text/plain", "x".getBytes());
    MockMultipartFile image = new MockMultipartFile("newAttachedImages", "i.jpg", "image/jpeg", "y".getBytes());

    CommentDto.CommentUpdateRequest request = CommentDto.CommentUpdateRequest.builder()
        .newAttachedFiles(List.of(file))
        .newAttachedImages(List.of(image))
        .build();

    // when
    var result = commentService.updateComment(10L, 100L, request, authorDetails);

    // then
    assertThat(result).isNotNull();
    verify(fileHandler, times(1)).saveFile(eq(file), any());
    verify(fileHandler, times(1)).saveFile(eq(image), any());
  }

  @Test
  @DisplayName("기존 첨부파일/이미지를 삭제할 수 있다")
  void updateComment_DeleteFilesAndImages() {
    // given
    when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(studyGroup));
    when(memberQueryRepository.isMember(studyGroup, author)).thenReturn(true);
    when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

    CommentAttachedFile existingFile = CommentAttachedFile.builder()
        .uuid("file-uuid")
        .fileName("file.txt")
        .build();
    comment.addAttachedFile(existingFile);

    CommentImageFile existingImage = CommentImageFile.builder()
        .uuid("img-uuid")
        .fileName("img.jpg")
        .build();
    comment.addAttachedImage(existingImage);

    CommentDto.CommentUpdateRequest request = CommentDto.CommentUpdateRequest.builder()
        .deletedAttachedFiles(List.of("file-uuid"))
        .deletedAttachedImages(List.of("img-uuid"))
        .build();

    // when
    var result = commentService.updateComment(10L, 100L, request, authorDetails);

    // then
    assertThat(result).isNotNull();
    verify(fileHandler, times(1)).deleteFile(existingFile);
    verify(fileHandler, times(1)).deleteFile(existingImage);
  }

  @Test
  @DisplayName("존재하지 않는 첨부 ID 삭제 시도시에도 예외 없이 통과한다")
  void updateComment_DeleteNonExisting_NoException() {
    // given
    when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(studyGroup));
    when(memberQueryRepository.isMember(studyGroup, author)).thenReturn(true);
    when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

    CommentDto.CommentUpdateRequest request = CommentDto.CommentUpdateRequest.builder()
        .deletedAttachedFiles(List.of("not-exist-file"))
        .deletedAttachedImages(List.of("not-exist-img"))
        .build();

    // when
    var result = commentService.updateComment(10L, 100L, request, authorDetails);

    // then
    assertThat(result).isNotNull();
    verify(fileHandler, never()).deleteFile(any());
  }

  @Test
  @DisplayName("스터디 그룹 멤버가 아니면 수정할 수 없다")
  void updateComment_NotMember_Forbidden() {
    // given
    when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(studyGroup));
    when(memberQueryRepository.isMember(studyGroup, author)).thenReturn(false);

    CommentDto.CommentUpdateRequest request = CommentDto.CommentUpdateRequest.builder()
        .content("updated")
        .build();

    // when & then
    assertThatThrownBy(() -> commentService.updateComment(10L, 100L, request, authorDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);
  }

  @Test
  @DisplayName("작성자가 아니고 관리자가 아니면 수정할 수 없다")
  void updateComment_NotAuthorAndNotAdmin_Forbidden() {
    // given
    when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(studyGroup));
    when(memberQueryRepository.isMember(studyGroup, anotherUser)).thenReturn(true);
    when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

    CommentDto.CommentUpdateRequest request = CommentDto.CommentUpdateRequest.builder()
        .content("updated")
        .build();

    // when & then
    assertThatThrownBy(() -> commentService.updateComment(10L, 100L, request, anotherDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);
  }

  @Test
  @DisplayName("관리자는 댓글을 수정할 수 있다")
  void updateComment_ByAdmin_Success() {
    // given
    when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(studyGroup));
    when(memberQueryRepository.isMember(studyGroup, adminUser)).thenReturn(true);
    when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

    CommentDto.CommentUpdateRequest request = CommentDto.CommentUpdateRequest.builder()
        .content("admin-updated")
        .build();

    // when
    var result = commentService.updateComment(10L, 100L, request, adminDetails);

    // then
    assertThat(result.getContent()).isEqualTo("admin-updated");
  }

  @Test
  @DisplayName("존재하지 않는 스터디 그룹이면 예외")
  void updateComment_StudyGroupNotFound() {
    // given
    when(studyGroupRepository.findById(999L)).thenReturn(Optional.empty());

    CommentDto.CommentUpdateRequest request = CommentDto.CommentUpdateRequest.builder()
        .content("u")
        .build();

    // when & then
    assertThatThrownBy(() -> commentService.updateComment(999L, 100L, request, authorDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_GROUP_NOT_FOUND);
  }

  @Test
  @DisplayName("존재하지 않는 댓글이면 예외")
  void updateComment_CommentNotFound() {
    // given
    when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(studyGroup));
    when(memberQueryRepository.isMember(studyGroup, author)).thenReturn(true);
    when(commentRepository.findById(100L)).thenReturn(Optional.empty());

    CommentDto.CommentUpdateRequest request = CommentDto.CommentUpdateRequest.builder()
        .content("u")
        .build();

    // when & then
    assertThatThrownBy(() -> commentService.updateComment(10L, 100L, request, authorDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
  }

  @Test
  @DisplayName("내용이 null이면 기존 내용 유지")
  void updateComment_NullContent_KeepsOriginal() {
    // given
    when(studyGroupRepository.findById(10L)).thenReturn(Optional.of(studyGroup));
    when(memberQueryRepository.isMember(studyGroup, author)).thenReturn(true);
    when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

    CommentDto.CommentUpdateRequest request = CommentDto.CommentUpdateRequest.builder()
        .content(null)
        .build();

    // when
    var result = commentService.updateComment(10L, 100L, request, authorDetails);

    // then
    assertThat(result.getContent()).isEqualTo("original content");
  }

  @Test
  @DisplayName("답변 작성자가 자신의 답변을 삭제할 수 있다")
  void deleteComment_ByAuthor_Success() {
    // given
    when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

    // when
    commentService.deleteComment(100L, authorDetails);

    // then
    verify(commentRepository).delete(comment);
  }

  @Test
  @DisplayName("스터디 그룹 주최자가 답변을 삭제할 수 있다")
  void deleteComment_ByOwner_Success() {
    // given
    when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

    // when
    commentService.deleteComment(100L, authorDetails);

    // then
    verify(commentRepository).delete(comment);
  }

  @Test
  @DisplayName("관리자가 답변을 삭제할 수 있다")
  void deleteComment_ByAdmin_Success() {
    // given
    when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

    // when
    commentService.deleteComment(100L, adminDetails);

    // then
    verify(commentRepository).delete(comment);
  }

  @Test
  @DisplayName("답변 작성자도 아니고 그룹 주최자도 아닌 사용자가 삭제 요청시 403 에러 발생")
  void deleteComment_NotAuthorAndNotOwner_Forbidden() {
    // given
    when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

    // when & then
    assertThatThrownBy(() -> commentService.deleteComment(100L, anotherDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);
  }

  @Test
  @DisplayName("존재하지 않는 답변이면 삭제 시도시 예외 발생")
  void deleteComment_CommentNotFound() {
    // given
    when(commentRepository.findById(999L)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> commentService.deleteComment(999L, authorDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
  }

  @Test
  @DisplayName("답변 삭제시 첨부 파일들도 함께 삭제된다")
  void deleteComment_WithAttachments_DeleteFiles() {
    // given
    CommentAttachedFile attachedFile = CommentAttachedFile.builder()
        .uuid("file-uuid")
        .fileName("file.txt")
        .build();
    comment.addAttachedFile(attachedFile);

    CommentImageFile attachedImage = CommentImageFile.builder()
        .uuid("img-uuid")
        .fileName("img.jpg")
        .build();
    comment.addAttachedImage(attachedImage);

    when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

    // when
    commentService.deleteComment(100L, authorDetails);

    // then
    verify(fileHandler, times(1)).deleteFile(attachedFile);
    verify(fileHandler, times(1)).deleteFile(attachedImage);
    verify(commentRepository).delete(comment);
  }
}
