package com.depth.learningcrew.domain.studygroup.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.depth.learningcrew.domain.studygroup.entity.Application;
import com.depth.learningcrew.domain.studygroup.entity.ApplicationId;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.user.entity.User;

public interface ApplicationRepository extends JpaRepository<Application, ApplicationId> {

  Optional<Application> findById_UserAndId_StudyGroup(User user, StudyGroup studyGroup);

  boolean existsById_UserAndId_StudyGroup(User user, StudyGroup studyGroup);

  Optional<Application> findById_User_IdAndId_StudyGroup_Id(Integer userId, Integer groupId);
}
