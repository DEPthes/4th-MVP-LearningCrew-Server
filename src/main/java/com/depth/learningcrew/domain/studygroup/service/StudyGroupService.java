package com.depth.learningcrew.domain.studygroup.service;

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

  @Transactional(readOnly = true)
  public PagedModel<StudyGroupDto.StudyGroupPaginationResponse> paginateMyOwnedStudyGroups(
      StudyGroupDto.SearchConditions searchConditions,
      UserDetails user,
      Pageable pageable) {
    Page<StudyGroupDto.StudyGroupPaginationResponse> result = studyGroupQueryRepository
        .paginateMyOwnedGroups(searchConditions, user, pageable);

    return new PagedModel<>(result);
  }
}
