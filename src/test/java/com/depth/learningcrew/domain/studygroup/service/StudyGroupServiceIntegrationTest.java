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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.depth.learningcrew.domain.file.entity.StudyGroupImage;
import com.depth.learningcrew.domain.studygroup.dto.StudyGroupDto;
import com.depth.learningcrew.domain.studygroup.entity.Application;
import com.depth.learningcrew.domain.studygroup.entity.ApplicationId;
import com.depth.learningcrew.domain.studygroup.entity.Dibs;
import com.depth.learningcrew.domain.studygroup.entity.DibsId;
import com.depth.learningcrew.domain.studygroup.entity.GroupCategory;
import com.depth.learningcrew.domain.studygroup.entity.Member;
import com.depth.learningcrew.domain.studygroup.entity.MemberId;
import com.depth.learningcrew.domain.studygroup.entity.State;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.entity.StudyStep;
import com.depth.learningcrew.domain.studygroup.entity.StudyStepId;
import com.depth.learningcrew.domain.studygroup.repository.ApplicationRepository;
import com.depth.learningcrew.domain.studygroup.repository.DibsRepository;
import com.depth.learningcrew.domain.studygroup.repository.GroupCategoryRepository;
import com.depth.learningcrew.domain.studygroup.repository.MemberRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyStepRepository;
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

  @Autowired
  private GroupCategoryRepository groupCategoryRepository;

  @Autowired
  private StudyGroupRepository studyGroupRepository;

  @Autowired
  private StudyStepRepository studyStepRepository;

  @Autowired
  private DibsRepository dibsRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private ApplicationRepository applicationRepository;

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
        studyGroup.getId(), request, ownerDetails);

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
        999L, request, ownerDetails))
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
        studyGroup.getId(), request, otherUserDetails))
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
        studyGroup.getId(), request, ownerDetails))
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
        studyGroup.getId(), request, ownerDetails);

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
        studyGroup.getId(), request, ownerDetails);

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
        studyGroup.getId(), request, ownerDetails);

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
        studyGroup.getId(), request, ownerDetails);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getDibs()).isFalse();
  }

  @Test
  @DisplayName("스터디 그룹을 생성할 수 있다")
  void createStudyGroup_ShouldCreateSuccessfully() {
    // given
    MockMultipartFile groupImage = new MockMultipartFile(
        "groupImage",
        "test-image.jpg",
        "image/jpeg",
        "dummy image content".getBytes());

    StudyGroupDto.StudyGroupCreateRequest request = StudyGroupDto.StudyGroupCreateRequest.builder()
        .name("새로운 스터디")
        .summary("스터디 요약")
        .maxMembers(5)
        .startDate(LocalDate.of(2025, 8, 1))
        .endDate(LocalDate.of(2025, 8, 20)) // steps의 마지막 날짜와 일치하도록 수정
        .categories(List.of("디자인", "프로그래밍"))
        .groupImage(groupImage)
        .steps(List.of(
            LocalDate.of(2025, 8, 15),
            LocalDate.of(2025, 8, 20) // 마지막 날짜
        ))
        .build();

    // when
    StudyGroupDto.StudyGroupDetailResponse response = studyGroupService.createStudyGroup(request, ownerDetails);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getName()).isEqualTo("새로운 스터디");
    assertThat(response.getSummary()).isEqualTo("스터디 요약");
    assertThat(response.getMaxMembers()).isEqualTo(5);
    assertThat(response.getOwner().getId()).isEqualTo(owner.getId());

    StudyGroup savedGroup = entityManager.find(StudyGroup.class, response.getId());
    assertThat(savedGroup).isNotNull();
    assertThat(savedGroup.getSteps()).hasSize(2);
    assertThat(savedGroup.getCategories()).hasSize(2);

    assertThat(programmingCategory.getStudyGroups()).anyMatch(g -> g.getId().equals(response.getId()));
    assertThat(designCategory.getStudyGroups()).anyMatch(g -> g.getId().equals(response.getId()));

    StudyGroupImage savedImage = savedGroup.getStudyGroupImage();
    assertThat(savedImage).isNotNull();
    assertThat(savedImage.getFileName()).isEqualTo("test-image.jpg");
    assertThat(savedImage.getSize()).isEqualTo(groupImage.getSize());
    assertThat(savedImage.getUuid()).isNotBlank();

  }

  @Test
  @DisplayName("새로운 카테고리를 포함해 스터디 그룹을 생성할 수 있다")
  void createStudyGroup_WithNewCategories_ShouldCreateSuccessfully() {
    // given
    String newCategory1 = "새로운 카테고리";

    StudyGroupDto.StudyGroupCreateRequest request = StudyGroupDto.StudyGroupCreateRequest.builder()
        .name("새로운 카테고리 스터디")
        .summary("새로운 카테고리 테스트")
        .maxMembers(8)
        .startDate(LocalDate.of(2025, 9, 1))
        .endDate(LocalDate.of(2025, 9, 20)) // steps의 마지막 날짜와 일치하도록 수정
        .categories(List.of(newCategory1))
        .steps(List.of(
            LocalDate.of(2025, 9, 10),
            LocalDate.of(2025, 9, 20) // 마지막 날짜
        ))
        .build();

    // when
    StudyGroupDto.StudyGroupDetailResponse response = studyGroupService.createStudyGroup(request, ownerDetails);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getName()).isEqualTo("새로운 카테고리 스터디");
    assertThat(response.getCategories()).hasSize(1);
    assertThat(response.getSteps()).hasSize(2);

    StudyGroup savedGroup = entityManager.find(StudyGroup.class, response.getId());
    assertThat(savedGroup).isNotNull();
    assertThat(savedGroup.getCategories()).hasSize(1);

    GroupCategory savedCategory1 = groupCategoryRepository.findByName(newCategory1).orElse(null);
    assertThat(savedCategory1).isNotNull();
    assertThat(savedCategory1.getStudyGroups()).anyMatch(g -> g.getId().equals(savedGroup.getId()));
  }

  @Test
  @DisplayName("스터디 그룹 삭제 시 연관된 step, dibs, member, application도 함께 삭제된다")
  void deleteStudyGroup_ShouldCascadeDeleteRelations() {
    // given
    // 찜
    Dibs dibs = Dibs.builder()
        .id(DibsId.of(owner, studyGroup))
        .build();
    entityManager.persist(dibs);

    // 멤버
    Member member = Member.builder()
        .id(MemberId.of(owner, studyGroup))
        .build();
    entityManager.persist(member);

    // 스텝
    StudyStep step1 = StudyStep.builder()
        .id(StudyStepId.of(1, studyGroup))
        .endDate(LocalDate.now().plusDays(1))
        .build();
    entityManager.persist(step1);

    // 신청
    Application application = Application.builder()
        .id(ApplicationId.of(otherUser, studyGroup))
        .state(State.APPROVED)
        .build();
    entityManager.persist(application);

    Member member2 = Member.builder()
        .id(MemberId.of(otherUser, studyGroup))
        .build();
    entityManager.persist(member2);

    entityManager.flush();
    entityManager.clear();

    // when
    studyGroupService.deleteStudyGroup(studyGroup.getId(), ownerDetails);

    // then
    assertThat(studyGroupRepository.findById(studyGroup.getId())).isEmpty();
    assertThat(studyStepRepository.findAll()).isEmpty();
    assertThat(dibsRepository.findAll()).isEmpty();
    assertThat(memberRepository.findAll()).isEmpty();
    assertThat(applicationRepository.findAll()).isEmpty();
  }
}