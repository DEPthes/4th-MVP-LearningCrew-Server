package com.depth.learningcrew.domain.studygroup.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.depth.learningcrew.domain.studygroup.dto.MemberDto;
import com.depth.learningcrew.domain.studygroup.entity.Member;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.MemberQueryRepository;
import com.depth.learningcrew.domain.studygroup.repository.MemberRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.domain.user.repository.UserRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberQueryRepository memberQueryRepository;
  private final MemberRepository memberRepository;
  private final StudyGroupRepository studyGroupRepository;
  private final UserRepository userRepository;

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

  @Transactional
  public void expelMember(Long groupId, Long userId, UserDetails userDetails) {
    StudyGroup studyGroup = studyGroupRepository.findById(groupId)
        .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

    User userToExpel = userRepository.findById(userId)
        .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

    cannotExpelIfYouAreNotOwner(userDetails, studyGroup, userToExpel);
    cannotExpelToOwner(userDetails, studyGroup, userToExpel);

    Member member = memberRepository.findById_UserAndId_StudyGroup(userToExpel, studyGroup)
        .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

    memberRepository.delete(member);

    // 멤버 수 감소
    studyGroup.decreaseMemberCount();
  }

  @Transactional
  public void leaveMember(Long groupId, UserDetails userDetails) {
    StudyGroup studyGroup = studyGroupRepository.findById(groupId)
        .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

    User user = userDetails.getUser();

    cannotLeaveIfOwner(user, studyGroup);

    Member member = memberRepository.findById_UserAndId_StudyGroup(user, studyGroup)
        .orElseThrow(() -> new RestException(ErrorCode.AUTH_FORBIDDEN));

    memberRepository.delete(member);

    // 멤버 수 감소
    studyGroup.decreaseMemberCount();
  }

  private void cannotViewIfNotOwner(UserDetails userDetails, StudyGroup studyGroup) {
    if (!studyGroup.getOwner().getId().equals(userDetails.getUser().getId())) {
      throw new RestException(ErrorCode.AUTH_FORBIDDEN);
    }
  }

  private void cannotExpelIfYouAreNotOwner(UserDetails userDetails, StudyGroup studyGroup, User userToExpel) {
    // owner가 아닌 경우 추방 불가
    if (!studyGroup.getOwner().getId().equals(userDetails.getUser().getId())) {
      throw new RestException(ErrorCode.AUTH_FORBIDDEN);
    }
  }

  private void cannotExpelToOwner(UserDetails userDetails, StudyGroup studyGroup, User userToExpel) {
    if (studyGroup.getOwner().getId().equals(userToExpel.getId())) {
      throw new RestException(ErrorCode.STUDY_GROUP_OWNER_CANNOT_BE_EXPELLED);
    }
  }

  private void cannotLeaveIfOwner(User user, StudyGroup studyGroup) {
    if (studyGroup.getOwner().getId().equals(user.getId())) {
      throw new RestException(ErrorCode.AUTH_FORBIDDEN);
    }
  }
}
