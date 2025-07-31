package com.depth.learningcrew.domain.studygroup.service;

import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.DibsRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.depth.learningcrew.domain.studygroup.dto.StudyGroupDto;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupQueryRepository;
import com.depth.learningcrew.system.security.model.UserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudyGroupService {

  private final StudyGroupQueryRepository studyGroupQueryRepository;
  private final DibsRepository dibsRepository;

  @Transactional(readOnly = true)
  public PagedModel<StudyGroupDto.StudyGroupPaginationResponse> paginateMyOwnedStudyGroups(
      StudyGroupDto.SearchConditions searchConditions,
      UserDetails user,
      Pageable pageable) {
    Page<StudyGroupDto.StudyGroupPaginationResponse> result = studyGroupQueryRepository
        .paginateMyOwnedGroups(searchConditions, user, pageable);

    return new PagedModel<>(result);
  }

    @Transactional(readOnly = true)
    public StudyGroupDto.StudyGroupDetailResponse getStudyGroupDetail(
            Integer groupId,
            UserDetails user) {
        StudyGroup studyGroup = studyGroupQueryRepository.findDetailById(groupId)
                .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

      boolean dibs = dibsRepository.existsByIdUserAndIdStudyGroup(user.getUser(), studyGroup);

      return StudyGroupDto.StudyGroupDetailResponse.from(studyGroup, dibs);
    }
}
