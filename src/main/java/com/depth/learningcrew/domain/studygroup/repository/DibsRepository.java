package com.depth.learningcrew.domain.studygroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.depth.learningcrew.domain.studygroup.entity.Dibs;
import com.depth.learningcrew.domain.studygroup.entity.DibsId;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.user.entity.User;

public interface DibsRepository extends JpaRepository<Dibs, DibsId> {
  boolean existsById_UserAndId_StudyGroup(User user, StudyGroup studyGroup);
}
