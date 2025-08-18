package com.depth.learningcrew.domain.studygroup.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.depth.learningcrew.domain.file.entity.StudyGroupImage;
import com.depth.learningcrew.domain.file.handler.FileHandler;
import com.depth.learningcrew.domain.studygroup.dto.StudyGroupDto;
import com.depth.learningcrew.domain.studygroup.entity.GroupCategory;
import com.depth.learningcrew.domain.studygroup.entity.Member;
import com.depth.learningcrew.domain.studygroup.entity.MemberId;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.entity.StudyStep;
import com.depth.learningcrew.domain.studygroup.entity.StudyStepId;
import com.depth.learningcrew.domain.studygroup.repository.DibsRepository;
import com.depth.learningcrew.domain.studygroup.repository.MemberRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupQueryRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudyGroupService {

  private final StudyGroupQueryRepository studyGroupQueryRepository;
  private final StudyGroupRepository studyGroupRepository;
  private final GroupCategoryService groupCategoryService;
  private final FileHandler fileHandler;
  private final DibsRepository dibsRepository;
  private final MemberRepository memberRepository;

  @Transactional
  public void updateStudyGroupCurrentStep() {
    List<StudyGroup> studyGroups = studyGroupQueryRepository.findStudyGroupsToUpdateStep();
    for (StudyGroup studyGroup : studyGroups) {
      studyGroup.setCurrentStep(studyGroup.getCurrentStep() + 1);
    }
  }

  @Transactional(readOnly = true)
  public PagedModel<StudyGroupDto.StudyGroupResponse> paginateMyOwnedStudyGroups(
      StudyGroupDto.SearchConditions searchConditions,
      UserDetails user,
      Pageable pageable) {
    Page<StudyGroupDto.StudyGroupResponse> result = studyGroupQueryRepository
        .paginateMyOwnedGroups(searchConditions, user, pageable);

    return new PagedModel<>(result);
  }

  @Transactional
  public StudyGroupDto.StudyGroupResponse updateStudyGroup(
      Long groupId,
      StudyGroupDto.StudyGroupUpdateRequest request,
      UserDetails userDetails) {
    StudyGroup group = studyGroupRepository.findById(groupId)
        .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

    group.canUpdateBy(userDetails);

    // 카테고리 처리
    List<GroupCategory> categories = null;
    if (request.getCategories() != null) {
      categories = groupCategoryService.findByNames(request.getCategories());
    }

    request.applyTo(group, categories);

    // 이미지 파일 처리
    if (request.getGroupImage() != null && !request.getGroupImage().isEmpty()) {
      if (group.getStudyGroupImage() != null) {
        fileHandler.deleteFile(group.getStudyGroupImage());
      }

      StudyGroupImage newImage = StudyGroupImage.from(request.getGroupImage(), group);
      fileHandler.saveFile(request.getGroupImage(), newImage);
      group.setStudyGroupImage(newImage);
    }

    boolean dibs = dibsRepository.existsById_UserAndId_StudyGroup(userDetails.getUser(), group);
    return StudyGroupDto.StudyGroupResponse.from(group, dibs);
  }

  @Transactional(readOnly = true)
  public StudyGroupDto.StudyGroupDetailResponse getStudyGroupDetail(
      Long groupId,
      @Nullable UserDetails user) {
    StudyGroup studyGroup = studyGroupQueryRepository.findDetailById(groupId)
        .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

    // user가 null이면 false, 아니면 dibs 여부 확인
    Boolean dibs = user != null && dibsRepository.existsById_UserAndId_StudyGroup(user.getUser(), studyGroup);
    return StudyGroupDto.StudyGroupDetailResponse.from(studyGroup, dibs);
  }

  @Transactional(readOnly = true)
  public PagedModel<StudyGroupDto.StudyGroupResponse> paginateAllStudyGroups(
      StudyGroupDto.SearchConditions searchConditions,
      UserDetails user,
      Pageable pageable) {

    Page<StudyGroupDto.StudyGroupResponse> result = studyGroupQueryRepository.paginateAllGroups(
        searchConditions, user, pageable);

    return new PagedModel<>(result);
  }

  @Transactional
  public StudyGroupDto.StudyGroupDetailResponse createStudyGroup(
      StudyGroupDto.StudyGroupCreateRequest request,
      UserDetails user) {

    User owner = user.getUser();

    cannotCreateWithInvalidateSteps(request);

    StudyGroup studyGroup = request.toEntity();
    studyGroup.setOwner(owner);

    saveStudygroupImageFile(request, studyGroup);
    saveCateogires(request, studyGroup);

    StudyGroup savedGroup = studyGroupRepository.save(studyGroup);

    MemberId memberId = MemberId.of(owner, savedGroup);
    Member member = new Member(memberId);
    memberRepository.save(member);

    saveIncludedSteps(request, savedGroup);

    return StudyGroupDto.StudyGroupDetailResponse.from(savedGroup, false);
  }

  private void cannotCreateWithInvalidateSteps(StudyGroupDto.StudyGroupCreateRequest request) {
    if (request.getSteps() == null || request.getSteps().isEmpty()) {
      return;
    }

    // 1. 중복된 일자가 있는지 확인 (먼저 체크)
    if (request.getSteps().size() != request.getSteps().stream().distinct().count()) {
      throw new RestException(ErrorCode.STUDY_GROUP_STEP_DUPLICATE_DATE);
    }

    // 2. steps 리스트를 일자별로 정렬
    List<LocalDate> sortedSteps = request.getSteps().stream()
        .sorted()
        .toList();

    // 3. step의 마지막 일자와 endDate가 일치하는지 확인
    LocalDate lastStepDate = sortedSteps.get(sortedSteps.size() - 1);
    if (request.getEndDate() != null && !lastStepDate.equals(request.getEndDate())) {
      throw new RestException(ErrorCode.STUDY_GROUP_STEP_END_DATE_MISMATCH);
    }

    // 4. endDate가 null이면 step의 마지막 날짜로 설정
    if (request.getEndDate() == null) {
      request.setEndDate(lastStepDate);
    }
  }

  private static void saveIncludedSteps(StudyGroupDto.StudyGroupCreateRequest request, StudyGroup savedGroup) {
    if (request.getSteps() != null) {
      // 정렬된 steps 사용
      List<LocalDate> sortedSteps = request.getSteps().stream()
          .sorted()
          .toList();

      int stepNumber = 1;
      for (LocalDate endDate : sortedSteps) {
        StudyStepId stepId = StudyStepId.builder()
            .step(stepNumber++)
            .studyGroupId(savedGroup)
            .build();

        StudyStep step = StudyStep.builder()
            .id(stepId)
            .endDate(endDate)
            .title(null)
            .content(null)
            .build();
        savedGroup.getSteps().add(step);
      }
    }
  }

  private void saveCateogires(StudyGroupDto.StudyGroupCreateRequest request, StudyGroup studyGroup) {
    if (request.getCategories() != null) {
      List<GroupCategory> categories = groupCategoryService.findOrCreateByNames(request.getCategories());
      for (GroupCategory category : categories) {
        studyGroup.addCategory(category);
      }
    }
  }

  private void saveStudygroupImageFile(StudyGroupDto.StudyGroupCreateRequest request, StudyGroup studyGroup) {
    if (request.getGroupImage() != null && !request.getGroupImage().isEmpty()) {
      StudyGroupImage image = StudyGroupImage.from(request.getGroupImage(), studyGroup);
      fileHandler.saveFile(request.getGroupImage(), image);
      studyGroup.setStudyGroupImage(image);
    }
  }

  @Transactional(readOnly = true)
  public PagedModel<StudyGroupDto.StudyGroupResponse> paginateMyMemberedStudyGroups(
      StudyGroupDto.SearchConditions searchConditions,
      UserDetails user,
      Pageable pageable) {
    Page<StudyGroupDto.StudyGroupResponse> result = studyGroupQueryRepository.paginateMyMemberedGroups(
        searchConditions, user, pageable);
    return new PagedModel<>(result);
  }

  @Transactional
  public void deleteStudyGroup(Long groupId, UserDetails user) {
    StudyGroup studyGroup = studyGroupRepository.findById(groupId)
        .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

    studyGroup.canDeleteBy(user);

    if (studyGroup.getStudyGroupImage() != null) {
      fileHandler.deleteFile(studyGroup.getStudyGroupImage());
    }

    studyGroup.getSteps().forEach(studyStep -> {
      studyStep.getFiles().forEach(fileHandler::deleteFile);
      studyStep.getImages().forEach(fileHandler::deleteFile);
    });

    studyGroupRepository.delete(studyGroup);
  }
}
