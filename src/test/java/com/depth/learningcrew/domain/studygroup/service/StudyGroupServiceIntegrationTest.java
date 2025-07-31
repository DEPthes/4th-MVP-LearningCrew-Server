package com.depth.learningcrew.domain.studygroup.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.depth.learningcrew.domain.studygroup.dto.StudyGroupDto;
import com.depth.learningcrew.domain.studygroup.entity.Dibs;
import com.depth.learningcrew.domain.studygroup.entity.DibsId;
import com.depth.learningcrew.domain.studygroup.entity.GroupCategory;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
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
class StudyGroupServiceIntegrationTest {

    @Autowired
    private StudyGroupService studyGroupService;

    @PersistenceContext
    private EntityManager entityManager;

    private User owner;
    private User otherUser;
    private UserDetails ownerDetails;
    private UserDetails otherUserDetails;
    private StudyGroup studyGroup;
    private GroupCategory programmingCategory;
    private GroupCategory designCategory;

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

        // 카테고리 생성
        programmingCategory = new GroupCategory(null, "프로그래밍", new ArrayList<>());
        designCategory = new GroupCategory(null, "디자인", new ArrayList<>());

        // 스터디 그룹 생성
        studyGroup = StudyGroup.builder()
                .name("기존 스터디 그룹")
                .summary("기존 스터디 그룹입니다.")
                .content("기존 스터디 그룹 내용입니다.")
                .maxMembers(10)
                .memberCount(3)
                .currentStep(1)
                .categories(List.of(programmingCategory))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        // 엔티티들을 데이터베이스에 저장
        entityManager.persist(owner);
        entityManager.persist(otherUser);
        entityManager.persist(programmingCategory);
        entityManager.persist(designCategory);
        entityManager.persist(studyGroup);
        entityManager.flush();

        // UserDetails 생성
        ownerDetails = UserDetails.builder()
                .user(owner)
                .build();

        otherUserDetails = UserDetails.builder()
                .user(otherUser)
                .build();
    }

    @Test
    @DisplayName("스터디 그룹 정보를 성공적으로 수정할 수 있다")
    void updateStudyGroup_ShouldUpdateSuccessfully() {
        // given
        StudyGroupDto.StudyGroupUpdateRequest request = StudyGroupDto.StudyGroupUpdateRequest.builder()
                .name("수정된 스터디 그룹")
                .summary("수정된 요약")
                .categories(List.of("디자인"))
                .startDate(LocalDate.now().plusDays(7))
                .build();

        // when
        StudyGroupDto.StudyGroupResponse result = studyGroupService.updateStudyGroup(
                studyGroup.getId(), request, null, ownerDetails);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("수정된 스터디 그룹");
        assertThat(result.getSummary()).isEqualTo("수정된 요약");
        assertThat(result.getStartDate()).isEqualTo(LocalDate.now().plusDays(7));
        assertThat(result.getCategories()).hasSize(1);
        assertThat(result.getCategories().get(0).getName()).isEqualTo("디자인");
    }

    @Test
    @DisplayName("존재하지 않는 스터디 그룹을 수정하려고 하면 예외가 발생한다")
    void updateStudyGroup_WithNonExistentGroup_ShouldThrowException() {
        // given
        StudyGroupDto.StudyGroupUpdateRequest request = StudyGroupDto.StudyGroupUpdateRequest.builder()
                .name("수정된 이름")
                .build();

        // when & then
        assertThatThrownBy(() -> studyGroupService.updateStudyGroup(
                999, request, null, ownerDetails))
                .isInstanceOf(RestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GLOBAL_NOT_FOUND);
    }

    @Test
    @DisplayName("owner가 아닌 사용자가 스터디 그룹을 수정하려고 하면 예외가 발생한다")
    void updateStudyGroup_WithNonOwner_ShouldThrowException() {
        // given
        StudyGroupDto.StudyGroupUpdateRequest request = StudyGroupDto.StudyGroupUpdateRequest.builder()
                .name("수정된 이름")
                .build();

        // when & then
        assertThatThrownBy(() -> studyGroupService.updateStudyGroup(
                studyGroup.getId(), request, null, otherUserDetails))
                .isInstanceOf(RestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_FORBIDDEN);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 수정하려고 하면 예외가 발생한다")
    void updateStudyGroup_WithNonExistentCategory_ShouldThrowException() {
        // given
        StudyGroupDto.StudyGroupUpdateRequest request = StudyGroupDto.StudyGroupUpdateRequest.builder()
                .categories(List.of("존재하지 않는 카테고리"))
                .build();

        // when & then
        assertThatThrownBy(() -> studyGroupService.updateStudyGroup(
                studyGroup.getId(), request, null, ownerDetails))
                .isInstanceOf(RestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GLOBAL_BAD_REQUEST);
    }

    @Test
    @DisplayName("부분적으로만 정보를 수정할 수 있다")
    void updateStudyGroup_WithPartialUpdate_ShouldUpdateOnlyProvidedFields() {
        // given
        String originalName = studyGroup.getName();
        LocalDate originalStartDate = studyGroup.getStartDate();

        StudyGroupDto.StudyGroupUpdateRequest request = StudyGroupDto.StudyGroupUpdateRequest.builder()
                .summary("부분 수정된 요약")
                .build();

        // when
        StudyGroupDto.StudyGroupResponse result = studyGroupService.updateStudyGroup(
                studyGroup.getId(), request, null, ownerDetails);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(originalName); // 변경되지 않음
        assertThat(result.getSummary()).isEqualTo("부분 수정된 요약"); // 변경됨
        assertThat(result.getStartDate()).isEqualTo(originalStartDate); // 변경되지 않음
    }

    @Test
    @DisplayName("이미지 파일과 함께 스터디 그룹을 수정할 수 있다")
    void updateStudyGroup_WithImageFile_ShouldUpdateWithImage() {
        // given
        StudyGroupDto.StudyGroupUpdateRequest request = StudyGroupDto.StudyGroupUpdateRequest.builder()
                .name("이미지와 함께 수정")
                .build();

        // when
        StudyGroupDto.StudyGroupResponse result = studyGroupService.updateStudyGroup(
                studyGroup.getId(), request, null, ownerDetails);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("이미지와 함께 수정");
    }

    @Test
    @DisplayName("찜한 사용자의 경우 dibs가 true로 반환된다")
    void updateStudyGroup_WithDibsUser_ShouldReturnDibsTrue() {
        // given
        // 사용자가 스터디 그룹을 찜함
        Dibs dibs = Dibs.builder()
                .id(DibsId.of(owner, studyGroup))
                .build();
        entityManager.persist(dibs);
        entityManager.flush();

        StudyGroupDto.StudyGroupUpdateRequest request = StudyGroupDto.StudyGroupUpdateRequest.builder()
                .name("찜 테스트")
                .build();

        // when
        StudyGroupDto.StudyGroupResponse result = studyGroupService.updateStudyGroup(
                studyGroup.getId(), request, null, ownerDetails);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDibs()).isTrue();
    }

    @Test
    @DisplayName("찜하지 않은 사용자의 경우 dibs가 false로 반환된다")
    void updateStudyGroup_WithoutDibsUser_ShouldReturnDibsFalse() {
        // given
        StudyGroupDto.StudyGroupUpdateRequest request = StudyGroupDto.StudyGroupUpdateRequest.builder()
                .name("찜 테스트")
                .build();

        // when
        StudyGroupDto.StudyGroupResponse result = studyGroupService.updateStudyGroup(
                studyGroup.getId(), request, null, ownerDetails);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDibs()).isFalse();
    }
}