package com.depth.learningcrew.domain.studygroup.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.depth.learningcrew.domain.studygroup.dto.MemberDto;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.MemberQueryRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberQueryRepository memberQueryRepository;
  private final StudyGroupRepository studyGroupRepository;

  @Transactional(readOnly = true)
  public PagedModel<MemberDto.MemberResponse> paginateStudyGroupMembers(
      Long groupId,
      MemberDto.SearchConditions searchConditions,
      UserDetails userDetails,
      Pageable pageable) {

    StudyGroup studyGroup = studyGroupRepository.findById(groupId)
        .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

    cannotViewIfNotOwner(userDetails, studyGroup);

    Page<MemberDto.MemberResponse> result = memberQueryRepository.paginateStudyGroupMembers(
        studyGroup, searchConditions, pageable);

    return new PagedModel<>(result);
  }

  private static void cannotViewIfNotOwner(UserDetails userDetails, StudyGroup studyGroup) {
    if(studyGroup.getOwner().getId().equals(userDetails.getUser().getId())) {
        throw new RestException(ErrorCode.AUTH_FORBIDDEN);
    }
  }
}
