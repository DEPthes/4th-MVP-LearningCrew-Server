package com.depth.learningcrew.domain.file.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ProfileImageTest {

  @Test
  @DisplayName("유효한 MultipartFile로 ProfileImage를 생성할 수 있다")
  void from_WithValidMultipartFile_ShouldCreateProfileImage() {
    // given
    MockMultipartFile file = new MockMultipartFile(
        "profileImage",
        "test-image.jpg",
        "image/jpeg",
        "test image content".getBytes());

    // when
    ProfileImage profileImage = ProfileImage.from(file);

    // then
    assertThat(profileImage).isNotNull();
    assertThat(profileImage.getFileName()).isEqualTo("test-image.jpg");
    assertThat(profileImage.getHandlingType()).isEqualTo(HandlingType.IMAGE);
    assertThat(profileImage.getSize()).isEqualTo("test image content".getBytes().length);
    assertThat(profileImage.getUuid()).isNotNull();
    assertThat(profileImage.getUuid()).isNotEmpty();
  }

  @Test
  @DisplayName("null MultipartFile로 ProfileImage를 생성하면 null을 반환한다")
  void from_WithNullMultipartFile_ShouldReturnNull() {
    // given
    MockMultipartFile file = null;

    // when
    ProfileImage profileImage = ProfileImage.from(file);

    // then
    assertThat(profileImage).isNull();
  }

  @Test
  @DisplayName("빈 MultipartFile로 ProfileImage를 생성하면 null을 반환한다")
  void from_WithEmptyMultipartFile_ShouldReturnNull() {
    // given
    MockMultipartFile file = new MockMultipartFile(
        "profileImage",
        "",
        "image/jpeg",
        new byte[0]);

    // when
    ProfileImage profileImage = ProfileImage.from(file);

    // then
    assertThat(profileImage).isNull();
  }

  @Test
  @DisplayName("큰 파일 크기의 MultipartFile로 ProfileImage를 생성할 수 있다")
  void from_WithLargeFile_ShouldCreateProfileImage() {
    // given
    byte[] largeContent = new byte[1024 * 1024]; // 1MB
    MockMultipartFile file = new MockMultipartFile(
        "profileImage",
        "large-image.jpg",
        "image/jpeg",
        largeContent);

    // when
    ProfileImage profileImage = ProfileImage.from(file);

    // then
    assertThat(profileImage).isNotNull();
    assertThat(profileImage.getFileName()).isEqualTo("large-image.jpg");
    assertThat(profileImage.getSize()).isEqualTo(1024 * 1024);
    assertThat(profileImage.getHandlingType()).isEqualTo(HandlingType.IMAGE);
  }

  @Test
  @DisplayName("다양한 이미지 확장자로 ProfileImage를 생성할 수 있다")
  void from_WithDifferentImageExtensions_ShouldCreateProfileImage() {
    // given
    String[] extensions = { "jpg", "jpeg", "png", "gif", "bmp" };

    for (String extension : extensions) {
      MockMultipartFile file = new MockMultipartFile(
          "profileImage",
          "test-image." + extension,
          "image/" + extension,
          "test content".getBytes());

      // when
      ProfileImage profileImage = ProfileImage.from(file);

      // then
      assertThat(profileImage).isNotNull();
      assertThat(profileImage.getFileName()).isEqualTo("test-image." + extension);
      assertThat(profileImage.getHandlingType()).isEqualTo(HandlingType.IMAGE);
    }
  }

  @Test
  @DisplayName("파일명이 null인 MultipartFile로 ProfileImage를 생성할 수 있다")
  void from_WithNullFileName_ShouldCreateProfileImage() {
    // given
    MockMultipartFile file = new MockMultipartFile(
        "profileImage",
        null,
        "image/jpeg",
        "test content".getBytes());

    // when
    ProfileImage profileImage = ProfileImage.from(file);

    // then
    assertThat(profileImage).isNotNull();
    assertThat(profileImage.getFileName()).isEqualTo("");
    assertThat(profileImage.getHandlingType()).isEqualTo(HandlingType.IMAGE);
    assertThat(profileImage.getSize()).isEqualTo("test content".getBytes().length);
    assertThat(profileImage.getUuid()).isNotNull();
    assertThat(profileImage.getUuid()).isNotEmpty();
  }
}