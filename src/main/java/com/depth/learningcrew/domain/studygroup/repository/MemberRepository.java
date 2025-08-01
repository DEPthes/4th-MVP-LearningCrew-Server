package com.depth.learningcrew.domain.studygroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.depth.learningcrew.domain.studygroup.entity.Member;
import com.depth.learningcrew.domain.studygroup.entity.MemberId;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.user.entity.User;

public interface MemberRepository extends JpaRepository<Member, MemberId> {

  boolean existsById_UserAndId_StudyGroup(User user, StudyGroup studyGroup);

}
