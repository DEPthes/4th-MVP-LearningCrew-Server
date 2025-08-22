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

import com.depth.learningcrew.domain.file.handler.FileHandler;
import com.depth.learningcrew.domain.qna.dto.QAndADto;
import com.depth.learningcrew.domain.qna.entity.Comment;
import com.depth.learningcrew.domain.qna.entity.QAndA;
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
class QAndAServiceTest {

  @Mock
  private QAndARepository qAndARepository;

  @Mock
  private StudyGroupRepository studyGroupRepository;

  @Mock
  private MemberQueryRepository memberQueryRepository;

  @Mock
  private FileHandler fileHandler;

  @InjectMocks
  private QAndAService qAndAService;

  private User testUser;
  private UserDetails testUserDetails;
  private StudyGroup testStudyGroup;
  private QAndA testQAndA;
  private QAndA anotherQAndA;
  private QAndADto.QAndACreateRequest createRequest;

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
        .currentStep(1)
        .startDate(LocalDate.now())
        .endDate(LocalDate.now().plusMonths(3))
        .owner(testUser)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    // 테스트 Q&A 설정
    testQAndA = QAndA.builder()
        .id(1L)
        .title("테스트 질문")
        .content("테스트 질문 내용입니다.")
        .step(1)
        .studyGroup(testStudyGroup)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .createdBy(testUser)
        .lastModifiedBy(testUser)
        .attachedFiles(new java.util.ArrayList<>())
        .attachedImages(new java.util.ArrayList<>())
        .build();

    anotherQAndA = QAndA.builder()
        .id(2L)
        .title("다른 질문")
        .content("다른 질문 내용")
        .step(1)
        .studyGroup(testStudyGroup)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .createdBy(testUser)
        .lastModifiedBy(testUser)
        .attachedFiles(new java.util.ArrayList<>())
        .attachedImages(new java.util.ArrayList<>())
        .comments(new java.util.ArrayList<>())
        .build();

    // 테스트 요청 DTO 설정
    createRequest = QAndADto.QAndACreateRequest.builder()
        .title("테스트 질문")
        .content("테스트 질문 내용입니다.")
        .attachedFiles(List.of())
        .attachedImages(List.of())
        .build();
  }

  @Test
  @DisplayName("질문을 성공적으로 생성할 수 있다")
  void createQAndA_ShouldCreateSuccessfully() {
    // given
    when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(testStudyGroup));
    when(memberQueryRepository.isMember(testStudyGroup, testUser)).thenReturn(true);
    when(qAndARepository.save(any(QAndA.class))).thenReturn(testQAndA);

    // when
    QAndADto.QAndADetailResponse result = qAndAService.createQAndA(createRequest, 1L, 1, testUserDetails);

    // then
    verify(studyGroupRepository, times(1)).findById(1L);
    verify(memberQueryRepository, times(1)).isMember(testStudyGroup, testUser);
    verify(qAndARepository, times(1)).save(any(QAndA.class));

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(testQAndA.getId());
    assertThat(result.getTitle()).isEqualTo(testQAndA.getTitle());
    assertThat(result.getStep()).isEqualTo(testQAndA.getStep());
  }

  @Test
  @DisplayName("첨부 파일과 이미지가 포함된 질문을 생성할 수 있다")
  void createQAndA_WithAttachedFiles_ShouldCreateSuccessfully() {
    // given
    MockMultipartFile file1 = new MockMultipartFile("file1", "test1.pdf", "application/pdf", "test content".getBytes());
    MockMultipartFile image1 = new MockMultipartFile("image1", "test1.jpg", "image/jpeg", "test image".getBytes());

    QAndADto.QAndACreateRequest requestWithFiles = QAndADto.QAndACreateRequest.builder()
        .title("파일 첨부 질문")
        .content("파일이 첨부된 질문입니다.")
        .attachedFiles(List.of(file1))
        .attachedImages(List.of(image1))
        .build();

    when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(testStudyGroup));
    when(memberQueryRepository.isMember(testStudyGroup, testUser)).thenReturn(true);
    when(qAndARepository.save(any(QAndA.class))).thenReturn(testQAndA);

    // when
    QAndADto.QAndADetailResponse result = qAndAService.createQAndA(requestWithFiles, 1L, 1, testUserDetails);

    // then
    verify(fileHandler, times(1)).saveFile(eq(file1), any());
    verify(fileHandler, times(1)).saveFile(eq(image1), any());
    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("존재하지 않는 스터디 그룹에 질문을 생성하려고 하면 예외가 발생한다")
  void createQAndA_WithNonExistentStudyGroup_ShouldThrowException() {
    // given
    when(studyGroupRepository.findById(999L)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> qAndAService.createQAndA(createRequest, 999L, 1, testUserDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_GROUP_NOT_FOUND);
  }

  @Test
  @DisplayName("스터디 그룹 멤버가 아닌 사용자가 질문을 생성하려고 하면 예외가 발생한다")
  void createQAndA_WithNonMember_ShouldThrowException() {
    // given
    when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(testStudyGroup));
    when(memberQueryRepository.isMember(testStudyGroup, testUser)).thenReturn(false);

    // when & then
    assertThatThrownBy(() -> qAndAService.createQAndA(createRequest, 1L, 1, testUserDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_GROUP_NOT_MEMBER);
  }

  @Test
  @DisplayName("빈 첨부 파일 리스트로 질문을 생성할 수 있다")
  void createQAndA_WithEmptyAttachedFiles_ShouldCreateSuccessfully() {
    // given
    QAndADto.QAndACreateRequest requestWithEmptyFiles = QAndADto.QAndACreateRequest.builder()
        .title("빈 파일 질문")
        .content("첨부 파일이 없는 질문입니다.")
        .attachedFiles(List.of())
        .attachedImages(List.of())
        .build();

    when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(testStudyGroup));
    when(memberQueryRepository.isMember(testStudyGroup, testUser)).thenReturn(true);
    when(qAndARepository.save(any(QAndA.class))).thenReturn(testQAndA);

    // when
    QAndADto.QAndADetailResponse result = qAndAService.createQAndA(requestWithEmptyFiles, 1L, 1, testUserDetails);

    // then
    verify(fileHandler, times(0)).saveFile(any(), any());
    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("null 첨부 파일 리스트로 질문을 생성할 수 있다")
  void createQAndA_WithNullAttachedFiles_ShouldCreateSuccessfully() {
    // given
    QAndADto.QAndACreateRequest requestWithNullFiles = QAndADto.QAndACreateRequest.builder()
        .title("null 파일 질문")
        .content("첨부 파일이 null인 질문입니다.")
        .attachedFiles(null)
        .attachedImages(null)
        .build();

    when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(testStudyGroup));
    when(memberQueryRepository.isMember(testStudyGroup, testUser)).thenReturn(true);
    when(qAndARepository.save(any(QAndA.class))).thenReturn(testQAndA);

    // when
    QAndADto.QAndADetailResponse result = qAndAService.createQAndA(requestWithNullFiles, 1L, 1, testUserDetails);

    // then
    verify(fileHandler, times(0)).saveFile(any(), any());
    assertThat(result).isNotNull();
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

    QAndADto.QAndACreateRequest requestWithMultipleFiles = QAndADto.QAndACreateRequest.builder()
        .title("다중 파일 첨부 질문")
        .content("여러 파일이 첨부된 질문입니다.")
        .attachedFiles(List.of(file1, file2))
        .attachedImages(List.of(image1, image2))
        .build();

    when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(testStudyGroup));
    when(memberQueryRepository.isMember(testStudyGroup, testUser)).thenReturn(true);
    when(qAndARepository.save(any(QAndA.class))).thenReturn(testQAndA);

    // when
    QAndADto.QAndADetailResponse result = qAndAService.createQAndA(requestWithMultipleFiles, 1L, 1, testUserDetails);

    // then
    verify(fileHandler, times(4)).saveFile(any(), any());
    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("질문을 성공적으로 수정할 수 있다")
  void updateQAndA_ShouldUpdateSuccessfully() {
    // given
    QAndADto.QAndAUpdateRequest updateRequest = QAndADto.QAndAUpdateRequest.builder()
        .title("수정된 제목")
        .content("수정된 내용")
        .build();
    when(qAndARepository.findById(1L)).thenReturn(Optional.of(testQAndA));

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(1L, updateRequest, testUserDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("수정된 제목");
    assertThat(result.getContent()).isEqualTo("수정된 내용");
  }

  @Test
  @DisplayName("존재하지 않는 질문을 수정하려고 하면 예외가 발생한다")
  void updateQAndA_WithNonExistentQAndA_ShouldThrowException() {
    // given
    QAndADto.QAndAUpdateRequest updateRequest = QAndADto.QAndAUpdateRequest.builder()
        .title("수정된 제목")
        .content("수정된 내용")
        .build();
    when(qAndARepository.findById(999L)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> qAndAService.updateQAndA(999L, updateRequest, testUserDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QANDA_NOT_FOUND);
  }

  @Test
  @DisplayName("권한이 없는 사용자가 질문을 수정하려고 하면 예외가 발생한다")
  void updateQAndA_WithUnauthorizedUser_ShouldThrowException() {
    // given
    User otherUser = User.builder().id(2L).role(Role.USER).build();
    UserDetails otherUserDetails = UserDetails.builder().user(otherUser).build();
    QAndADto.QAndAUpdateRequest updateRequest = QAndADto.QAndAUpdateRequest.builder()
        .title("수정된 제목")
        .content("수정된 내용")
        .build();
    when(qAndARepository.findById(1L)).thenReturn(Optional.of(testQAndA));

    // when & then
    assertThatThrownBy(() -> qAndAService.updateQAndA(1L, updateRequest, otherUserDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QANDA_NOT_AUTHORIZED);
  }

  @Test
  @DisplayName("질문 제목만 수정할 수 있다")
  void updateQAndA_OnlyTitle_ShouldUpdateTitle() {
    // given
    QAndADto.QAndAUpdateRequest updateRequest = QAndADto.QAndAUpdateRequest.builder()
        .title("제목만 수정")
        .build();
    when(qAndARepository.findById(1L)).thenReturn(Optional.of(testQAndA));

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(1L, updateRequest, testUserDetails);

    // then
    assertThat(result.getTitle()).isEqualTo("제목만 수정");
    assertThat(result.getContent()).isEqualTo("테스트 질문 내용입니다.");
  }

  @Test
  @DisplayName("질문 내용만 수정할 수 있다")
  void updateQAndA_OnlyContent_ShouldUpdateContent() {
    // given
    QAndADto.QAndAUpdateRequest updateRequest = QAndADto.QAndAUpdateRequest.builder()
        .content("내용만 수정")
        .build();
    when(qAndARepository.findById(1L)).thenReturn(Optional.of(testQAndA));

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(1L, updateRequest, testUserDetails);

    // then
    assertThat(result.getTitle()).isEqualTo("테스트 질문");
    assertThat(result.getContent()).isEqualTo("내용만 수정");
  }

  @Test
  @DisplayName("null 값이 전달되어도 기존 값이 유지된다")
  void updateQAndA_WithNullValues_ShouldKeepOriginal() {
    // given
    QAndADto.QAndAUpdateRequest updateRequest = QAndADto.QAndAUpdateRequest.builder()
        .title(null)
        .content(null)
        .build();
    when(qAndARepository.findById(1L)).thenReturn(Optional.of(testQAndA));

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(1L, updateRequest, testUserDetails);

    // then
    assertThat(result.getTitle()).isEqualTo("테스트 질문");
    assertThat(result.getContent()).isEqualTo("테스트 질문 내용입니다.");
  }

  @Test
  @DisplayName("첨부 파일을 추가할 수 있다")
  void updateQAndA_AddNewAttachedFiles_ShouldSucceed() {
    // given
    MockMultipartFile file1 = new MockMultipartFile("newAttachedFiles", "file1.txt", "text/plain", "file1".getBytes());
    QAndADto.QAndAUpdateRequest updateRequest = QAndADto.QAndAUpdateRequest.builder()
        .newAttachedFiles(List.of(file1))
        .build();
    when(qAndARepository.findById(1L)).thenReturn(Optional.of(testQAndA));

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(1L, updateRequest, testUserDetails);

    // then
    verify(fileHandler, times(1)).saveFile(eq(file1), any());
    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("첨부 이미지를 추가할 수 있다")
  void updateQAndA_AddNewAttachedImages_ShouldSucceed() {
    // given
    MockMultipartFile image1 = new MockMultipartFile("newAttachedImages", "image1.jpg", "image/jpeg",
        "img1".getBytes());
    QAndADto.QAndAUpdateRequest updateRequest = QAndADto.QAndAUpdateRequest.builder()
        .newAttachedImages(List.of(image1))
        .build();
    when(qAndARepository.findById(1L)).thenReturn(Optional.of(testQAndA));

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(1L, updateRequest, testUserDetails);

    // then
    verify(fileHandler, times(1)).saveFile(eq(image1), any());
    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("첨부 파일을 삭제할 수 있다")
  void updateQAndA_DeleteAttachedFiles_ShouldSucceed() {
    // given
    // QAndA에 파일을 미리 추가
    var file = com.depth.learningcrew.domain.file.entity.QAndAAttachedFile.builder()
        .uuid("file-uuid-1").fileName("file1.txt").build();
    testQAndA.getAttachedFiles().add(file);
    QAndADto.QAndAUpdateRequest updateRequest = QAndADto.QAndAUpdateRequest.builder()
        .deletedAttachedFiles(List.of("file-uuid-1"))
        .build();
    when(qAndARepository.findById(1L)).thenReturn(Optional.of(testQAndA));

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(1L, updateRequest, testUserDetails);

    // then
    verify(fileHandler, times(1)).deleteFile(file);
    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("첨부 이미지를 삭제할 수 있다")
  void updateQAndA_DeleteAttachedImages_ShouldSucceed() {
    // given
    var image = com.depth.learningcrew.domain.file.entity.QAndAImageFile.builder()
        .uuid("img-uuid-1").fileName("img1.jpg").build();
    testQAndA.getAttachedImages().add(image);
    QAndADto.QAndAUpdateRequest updateRequest = QAndADto.QAndAUpdateRequest.builder()
        .deletedAttachedImages(List.of("img-uuid-1"))
        .build();
    when(qAndARepository.findById(1L)).thenReturn(Optional.of(testQAndA));

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(1L, updateRequest, testUserDetails);

    // then
    verify(fileHandler, times(1)).deleteFile(image);
    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("복합적으로 파일 추가/삭제, 이미지 추가/삭제를 동시에 할 수 있다")
  void updateQAndA_ComplexUpdate_ShouldSucceed() {
    // given
    var file = com.depth.learningcrew.domain.file.entity.QAndAAttachedFile.builder()
        .uuid("file-uuid-1").fileName("file1.txt").build();
    testQAndA.getAttachedFiles().add(file);
    var image = com.depth.learningcrew.domain.file.entity.QAndAImageFile.builder()
        .uuid("img-uuid-1").fileName("img1.jpg").build();
    testQAndA.getAttachedImages().add(image);
    MockMultipartFile newFile = new MockMultipartFile("newAttachedFiles", "file2.txt", "text/plain",
        "file2".getBytes());
    MockMultipartFile newImage = new MockMultipartFile("newAttachedImages", "img2.jpg", "image/jpeg",
        "img2".getBytes());
    QAndADto.QAndAUpdateRequest updateRequest = QAndADto.QAndAUpdateRequest.builder()
        .newAttachedFiles(List.of(newFile))
        .newAttachedImages(List.of(newImage))
        .deletedAttachedFiles(List.of("file-uuid-1"))
        .deletedAttachedImages(List.of("img-uuid-1"))
        .build();
    when(qAndARepository.findById(1L)).thenReturn(Optional.of(testQAndA));

    // when
    QAndADto.QAndADetailResponse result = qAndAService.updateQAndA(1L, updateRequest, testUserDetails);

    // then
    verify(fileHandler, times(1)).deleteFile(file);
    verify(fileHandler, times(1)).deleteFile(image);
    verify(fileHandler, times(1)).saveFile(eq(newFile), any());
    verify(fileHandler, times(1)).saveFile(eq(newImage), any());
    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("질문 삭제 성공 시 첨부파일/이미지 및 댓글 이미지까지 파일 삭제 호출")
  void deleteQAndA_ShouldDeleteFilesAndCommentImages() {
    // given
    var file = com.depth.learningcrew.domain.file.entity.QAndAAttachedFile.builder()
        .uuid("file-uuid-1").fileName("file1.txt").build();
    var image = com.depth.learningcrew.domain.file.entity.QAndAImageFile.builder()
        .uuid("img-uuid-1").fileName("img1.jpg").build();
    testQAndA.getAttachedFiles().add(file);
    testQAndA.getAttachedImages().add(image);

    Comment comment = Comment.builder().content("c").build();
    var cimg = com.depth.learningcrew.domain.file.entity.CommentImageFile.builder()
        .uuid("cimg-1").fileName("cimg.png").build();
    comment.addAttachedImage(cimg);
    testQAndA.addComment(comment);

    when(qAndARepository.findById(1L)).thenReturn(Optional.of(testQAndA));

    // when
    qAndAService.deleteQAndA(1L, testUserDetails);

    // then
    verify(fileHandler, times(1)).deleteFile(file);
    verify(fileHandler, times(1)).deleteFile(image);
    verify(fileHandler, times(1)).deleteFile(cimg);
    verify(qAndARepository, times(1)).delete(testQAndA);
  }

  @Test
  @DisplayName("존재하지 않는 질문 삭제 시 예외 발생")
  void deleteQAndA_NotFound_ShouldThrow() {
    // given
    when(qAndARepository.findById(999L)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> qAndAService.deleteQAndA(999L, testUserDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QANDA_NOT_FOUND);

    verify(qAndARepository, never()).delete(any());
  }

  @Test
  @DisplayName("권한 없는 사용자 삭제 시 예외 발생")
  void deleteQAndA_Unauthorized_ShouldThrow() {
    // given: 다른 사용자 생성
    User otherUser = User.builder().id(2L).role(Role.USER).build();
    UserDetails otherDetails = UserDetails.builder().user(otherUser).build();

    when(qAndARepository.findById(1L)).thenReturn(Optional.of(testQAndA));

    // when & then
    assertThatThrownBy(() -> qAndAService.deleteQAndA(1L, otherDetails))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.QANDA_NOT_AUTHORIZED);

    verify(qAndARepository, never()).delete(any());
  }
}
