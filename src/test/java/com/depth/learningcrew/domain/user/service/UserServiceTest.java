package com.depth.learningcrew.domain.user.service;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import com.depth.learningcrew.domain.file.entity.HandlingType;
import com.depth.learningcrew.domain.file.entity.ProfileImage;
import com.depth.learningcrew.domain.file.handler.FileHandler;
import com.depth.learningcrew.domain.file.repository.AttachedFileRepository;
import com.depth.learningcrew.domain.user.dto.UserDto;
import com.depth.learningcrew.domain.user.entity.Gender;
import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.domain.user.repository.UserRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private FileHandler fileHandler;

  @Mock
  private AttachedFileRepository attachedFileRepository;

  @InjectMocks
  private UserService userService;

  private User testUser;
  private UserDto.UserUpdateRequest updateRequest;

  @BeforeEach
  void setUp() {
    testUser = User.builder()
        .id(1L)
        .email("test@example.com")
        .password("encodedPassword")
        .nickname("testUser")
        .birthday(LocalDate.of(1990, 1, 1))
        .gender(Gender.MALE)
        .role(Role.USER)
        .createdAt(LocalDateTime.now())
        .lastModifiedAt(LocalDateTime.now())
        .build();

    updateRequest = UserDto.UserUpdateRequest.builder()
        .email("test@example.com")
        .nickname("testUser")
        .build();
  }

  @Test
  @DisplayName("프로필 이미지가 없는 사용자에게 새로운 프로필 이미지를 업로드할 수 있다")
  void updateProfileImage_WhenUserHasNoProfileImage_ShouldCreateNewProfileImage() {
    // given
    MockMultipartFile newImage = new MockMultipartFile(
        "profileImage",
        "test-image.jpg",
        "image/jpeg",
        "test image content".getBytes());

    updateRequest = UserDto.UserUpdateRequest.builder()
        .profileImage(newImage)
        .build();

    when(userRepository.findById(testUser.getId())).thenReturn(java.util.Optional.of(testUser));
    when(attachedFileRepository.save(any(ProfileImage.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    UserDto.UserUpdateResponse response = userService.update(testUser, updateRequest);

    // then
    verify(fileHandler, times(1)).saveFile(eq(newImage), any(ProfileImage.class));
    verify(fileHandler, never()).deleteFile(any(ProfileImage.class));

    assertThat(response).isNotNull();
    assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
    assertThat(response.getNickname()).isEqualTo(testUser.getNickname());
  }

  @Test
  @DisplayName("기존 프로필 이미지가 있는 사용자가 새로운 프로필 이미지를 업로드하면 기존 이미지를 삭제하고 새 이미지를 저장한다")
  void updateProfileImage_WhenUserHasExistingProfileImage_ShouldDeleteOldAndSaveNew() {
    // given
    ProfileImage existingProfileImage = ProfileImage.builder()
        .uuid("existing-uuid")
        .fileName("existing-image.jpg")
        .handlingType(HandlingType.IMAGE)
        .size(1024L)
        .user(testUser)
        .build();

    testUser.setProfileImage(existingProfileImage);

    MockMultipartFile newImage = new MockMultipartFile(
        "profileImage",
        "new-test-image.jpg",
        "image/jpeg",
        "new test image content".getBytes());

    updateRequest = UserDto.UserUpdateRequest.builder()
        .profileImage(newImage)
        .build();

    when(userRepository.findById(testUser.getId())).thenReturn(java.util.Optional.of(testUser));
    when(attachedFileRepository.save(any(ProfileImage.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    UserDto.UserUpdateResponse response = userService.update(testUser, updateRequest);

    // then
    verify(fileHandler, times(1)).deleteFile(existingProfileImage);
    verify(fileHandler, times(1)).saveFile(eq(newImage), any(ProfileImage.class));

    assertThat(response).isNotNull();
    assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
    assertThat(response.getNickname()).isEqualTo(testUser.getNickname());
  }

  @Test
  @DisplayName("프로필 이미지 없이 다른 정보만 업데이트할 때는 파일 관련 작업을 수행하지 않는다")
  void updateUserInfo_WithoutProfileImage_ShouldNotPerformFileOperations() {
    // given
    updateRequest = UserDto.UserUpdateRequest.builder()
        .nickname("newNickname")
        .email("newemail@example.com")
        .build();

    when(userRepository.findById(testUser.getId())).thenReturn(java.util.Optional.of(testUser));
    when(userRepository.existsByNickname("newNickname")).thenReturn(false);
    when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);

    // when
    UserDto.UserUpdateResponse response = userService.update(testUser, updateRequest);

    // then
    verify(fileHandler, never()).saveFile(any(MultipartFile.class), any(ProfileImage.class));
    verify(fileHandler, never()).deleteFile(any(ProfileImage.class));

    assertThat(response).isNotNull();
    assertThat(response.getEmail()).isEqualTo("newemail@example.com");
    assertThat(response.getNickname()).isEqualTo("newNickname");
  }

  @Test
  @DisplayName("빈 프로필 이미지 파일이 전달되면 ProfileImage.from()에서 null을 반환하므로 파일 작업을 수행하지 않는다")
  void updateProfileImage_WithEmptyFile_ShouldNotPerformFileOperations() {
    // given
    MockMultipartFile emptyImage = new MockMultipartFile(
        "profileImage",
        "",
        "image/jpeg",
        new byte[0]);

    updateRequest = UserDto.UserUpdateRequest.builder()
        .profileImage(emptyImage)
        .build();

    when(userRepository.findById(testUser.getId())).thenReturn(java.util.Optional.of(testUser));

    // when
    UserDto.UserUpdateResponse response = userService.update(testUser, updateRequest);

    // then
    verify(fileHandler, never()).saveFile(any(MultipartFile.class), any(ProfileImage.class));
    verify(fileHandler, never()).deleteFile(any(ProfileImage.class));

    assertThat(response).isNotNull();
  }

  @Test
  @DisplayName("사용자를 찾을 수 없으면 RestException이 발생한다")
  void updateUser_WhenUserNotFound_ShouldThrowRestException() {
    // given
    when(userRepository.findById(testUser.getId())).thenReturn(java.util.Optional.empty());

    // when & then
    assertThatThrownBy(() -> userService.update(testUser, updateRequest))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GLOBAL_NOT_FOUND);
  }

  @Test
  @DisplayName("중복된 닉네임으로 업데이트하려고 하면 RestException이 발생한다")
  void updateUser_WithDuplicateNickname_ShouldThrowRestException() {
    // given
    updateRequest = UserDto.UserUpdateRequest.builder()
        .nickname("duplicateNickname")
        .build();

    when(userRepository.findById(testUser.getId())).thenReturn(java.util.Optional.of(testUser));
    when(userRepository.existsByNickname("duplicateNickname")).thenReturn(true);

    // when & then
    assertThatThrownBy(() -> userService.update(testUser, updateRequest))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NICKNAME_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("중복된 이메일로 업데이트하려고 하면 RestException이 발생한다")
  void updateUser_WithDuplicateEmail_ShouldThrowRestException() {
    // given
    updateRequest = UserDto.UserUpdateRequest.builder()
        .email("duplicate@example.com")
        .build();

    when(userRepository.findById(testUser.getId())).thenReturn(java.util.Optional.of(testUser));
    when(userRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

    // when & then
    assertThatThrownBy(() -> userService.update(testUser, updateRequest))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_ALREADY_EMAIL_EXISTS);
  }

  @Test
  @DisplayName("비밀번호와 프로필 이미지를 함께 업데이트할 수 있다")
  void updateUser_WithPasswordAndProfileImage_ShouldUpdateBoth() {
    // given
    MockMultipartFile newImage = new MockMultipartFile(
        "profileImage",
        "test-image.jpg",
        "image/jpeg",
        "test image content".getBytes());

    updateRequest = UserDto.UserUpdateRequest.builder()
        .password("newPassword123")
        .profileImage(newImage)
        .build();

    when(userRepository.findById(testUser.getId())).thenReturn(java.util.Optional.of(testUser));
    when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
    when(attachedFileRepository.save(any(ProfileImage.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    UserDto.UserUpdateResponse response = userService.update(testUser, updateRequest);

    // then
    verify(passwordEncoder, times(1)).encode("newPassword123");
    verify(fileHandler, times(1)).saveFile(eq(newImage), any(ProfileImage.class));

    assertThat(response).isNotNull();
    assertThat(testUser.getPassword()).isEqualTo("encodedNewPassword");
  }
}