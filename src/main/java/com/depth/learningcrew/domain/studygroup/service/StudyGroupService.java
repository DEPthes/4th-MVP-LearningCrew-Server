package com.depth.learningcrew.domain.studygroup.service;

import java.time.LocalDate;
import java.util.List;

import com.depth.learningcrew.domain.studygroup.entity.*;
import com.depth.learningcrew.domain.studygroup.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.depth.learningcrew.domain.file.entity.StudyGroupImage;
import com.depth.learningcrew.domain.file.handler.FileHandler;
import com.depth.learningcrew.domain.studygroup.dto.StudyGroupDto;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudyGroupService {

  private final StudyGroupQueryRepository studyGroupQueryRepository;
  private final StudyGroupRepository studyGroupRepository;
  private final GroupCategoryService groupCategoryService;
  private final FileHandler fileHandler;
  private final DibsRepository dibsRepository;
  private final StudyStepRepository studyStepRepository;
  private final MemberRepository memberRepository;

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
      UserDetails user) {
    StudyGroup studyGroup = studyGroupQueryRepository.findDetailById(groupId)
        .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

    boolean dibs = dibsRepository.existsById_UserAndId_StudyGroup(user.getUser(), studyGroup);
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

    StudyGroup studyGroup = StudyGroup.builder()
        .name(request.getName())
        .summary(request.getSummary())
        .maxMembers(request.getMaxMembers())
        .memberCount(1)
        .currentStep(1)
        .startDate(request.getStartDate())
        .endDate(request.getEndDate())
        .owner(owner)
        .build();

    // 카테고리 처리
    if (request.getCategories() != null) {
      List<GroupCategory> categories = groupCategoryService.findOrCreateByNames(request.getCategories());
      for (GroupCategory category : categories) {
        studyGroup.addCategory(category);
      }
    }

    // 이미지 파일 처리
    if (request.getGroupImage() != null && !request.getGroupImage().isEmpty()) {
      StudyGroupImage image = StudyGroupImage.from(request.getGroupImage(), studyGroup);
      fileHandler.saveFile(request.getGroupImage(), image);
      studyGroup.setStudyGroupImage(image);
    }

    StudyGroup savedGroup = studyGroupRepository.save(studyGroup);

    MemberId memberId = MemberId.of(owner, savedGroup);
    Member member = new Member(memberId);
    memberRepository.save(member);

    // Step 저장
    if (request.getSteps() != null) {
      int stepNumber = 1;
      for (LocalDate endDate : request.getSteps()) {
        StudyStepId stepId = StudyStepId.builder()
            .step(stepNumber++)
            .studyGroupId(savedGroup)
            .build();

        StudyStep step = StudyStep.builder()
            .id(stepId)
            .endDate(endDate)
            .build();
        savedGroup.getSteps().add(step);
      }
    }

    return StudyGroupDto.StudyGroupDetailResponse.from(savedGroup, false);
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

    studyGroupRepository.delete(studyGroup);
  }
}
