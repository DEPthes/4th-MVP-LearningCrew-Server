package com.depth.learningcrew.domain.studygroup.service;

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
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.DibsRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupQueryRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
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
      Integer groupId,
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
}
