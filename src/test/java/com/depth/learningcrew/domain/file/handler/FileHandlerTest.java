package com.depth.learningcrew.domain.file.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.depth.learningcrew.domain.file.entity.HandlingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.depth.learningcrew.domain.file.entity.ProfileImage;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;

@ExtendWith(MockitoExtension.class)
class FileHandlerTest {

  @InjectMocks
  private FileHandler fileHandler;

  @TempDir
  Path tempDir;

  private ProfileImage testProfileImage;
  private MockMultipartFile testFile;

  @BeforeEach
  void setUp() {
    // 임시 디렉토리를 savePath로 설정
    ReflectionTestUtils.setField(fileHandler, "savePath", tempDir.toString());

    testProfileImage = ProfileImage.builder()
        .uuid("test-uuid-123")
        .fileName("test-image.jpg")
        .handlingType(HandlingType.IMAGE)
        .size(1024L)
        .build();

    testFile = new MockMultipartFile(
        "profileImage",
        "test-image.jpg",
        "image/jpeg",
        "test image content".getBytes());
  }

  @Test
  @DisplayName("유효한 파일을 저장할 수 있다")
  void saveFile_WithValidFile_ShouldSaveSuccessfully() {
    // given
    // tempDir는 이미 설정되어 있음

    // when
    fileHandler.saveFile(testFile, testProfileImage);

    // then
    File savedFile = tempDir.resolve(testProfileImage.getUuid()).toFile();
    assertThat(savedFile).exists();
    assertThat(savedFile.length()).isEqualTo("test image content".getBytes().length);
  }

  @Test
  @DisplayName("이미 존재하는 파일을 저장하려고 하면 RestException이 발생한다")
  void saveFile_WithExistingFile_ShouldThrowRestException() throws IOException {
    // given
    File existingFile = tempDir.resolve(testProfileImage.getUuid()).toFile();
    Files.write(existingFile.toPath(), "existing content".getBytes());

    // when & then
    assertThatThrownBy(() -> fileHandler.saveFile(testFile, testProfileImage))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_ALREADY_EXISTS);
  }

  @Test
  @DisplayName("저장된 파일을 Resource로 로드할 수 있다")
  void loadFileAsResource_WithExistingFile_ShouldReturnResource() throws IOException {
    // given
    File savedFile = tempDir.resolve(testProfileImage.getUuid()).toFile();
    Files.write(savedFile.toPath(), "test content".getBytes());

    // when
    Resource resource = fileHandler.loadFileAsResource(testProfileImage);

    // then
    assertThat(resource).isNotNull();
    assertThat(resource.exists()).isTrue();
    assertThat(resource.getFile().length()).isEqualTo("test content".getBytes().length);
  }

  @Test
  @DisplayName("존재하지 않는 파일을 로드하려고 하면 RestException이 발생한다")
  void loadFileAsResource_WithNonExistingFile_ShouldThrowRestException() {
    // given
    // 파일이 존재하지 않는 상태

    // when & then
    assertThatThrownBy(() -> fileHandler.loadFileAsResource(testProfileImage))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_FOUND);
  }

  @Test
  @DisplayName("존재하는 파일을 삭제할 수 있다")
  void deleteFile_WithExistingFile_ShouldDeleteSuccessfully() throws IOException {
    // given
    File savedFile = tempDir.resolve(testProfileImage.getUuid()).toFile();
    Files.write(savedFile.toPath(), "test content".getBytes());
    assertThat(savedFile).exists();

    // when
    fileHandler.deleteFile(testProfileImage);

    // then
    assertThat(savedFile).doesNotExist();
  }

  @Test
  @DisplayName("존재하지 않는 파일을 삭제하려고 하면 RestException이 발생한다")
  void deleteFile_WithNonExistingFile_ShouldThrowRestException() {
    // given
    // 파일이 존재하지 않는 상태

    // when & then
    assertThatThrownBy(() -> fileHandler.deleteFile(testProfileImage))
        .isInstanceOf(RestException.class)
        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_FOUND);
  }

  @Test
  @DisplayName("큰 파일을 저장할 수 있다")
  void saveFile_WithLargeFile_ShouldSaveSuccessfully() {
    // given
    byte[] largeContent = new byte[1024 * 1024]; // 1MB
    MockMultipartFile largeFile = new MockMultipartFile(
        "profileImage",
        "large-image.jpg",
        "image/jpeg",
        largeContent);

    ProfileImage largeProfileImage = ProfileImage.builder()
        .uuid("large-uuid-456")
        .fileName("large-image.jpg")
        .handlingType(HandlingType.IMAGE)
        .size((long) largeContent.length)
        .build();

    // when
    fileHandler.saveFile(largeFile, largeProfileImage);

    // then
    File savedFile = tempDir.resolve(largeProfileImage.getUuid()).toFile();
    assertThat(savedFile).exists();
    assertThat(savedFile.length()).isEqualTo(largeContent.length);
  }

  @Test
  @DisplayName("빈 파일을 저장할 수 있다")
  void saveFile_WithEmptyFile_ShouldSaveSuccessfully() {
    // given
    MockMultipartFile emptyFile = new MockMultipartFile(
        "profileImage",
        "empty-image.jpg",
        "image/jpeg",
        new byte[0]);

    ProfileImage emptyProfileImage = ProfileImage.builder()
        .uuid("empty-uuid-789")
        .fileName("empty-image.jpg")
        .handlingType(HandlingType.IMAGE)
        .size(0L)
        .build();

    // when
    fileHandler.saveFile(emptyFile, emptyProfileImage);

    // then
    File savedFile = tempDir.resolve(emptyProfileImage.getUuid()).toFile();
    assertThat(savedFile).exists();
    assertThat(savedFile.length()).isEqualTo(0);
  }

  @Test
  @DisplayName("디렉토리가 존재하지 않으면 자동으로 생성한다")
  void saveFile_WithNonExistingDirectory_ShouldCreateDirectory() {
    // given
    String nonExistingPath = tempDir.resolve("non-existing-dir").toString();
    ReflectionTestUtils.setField(fileHandler, "savePath", nonExistingPath);

    // when
    fileHandler.saveFile(testFile, testProfileImage);

    // then
    File savedFile = Path.of(nonExistingPath).resolve(testProfileImage.getUuid()).toFile();
    assertThat(savedFile).exists();
    assertThat(savedFile.getParentFile()).exists();
  }
}