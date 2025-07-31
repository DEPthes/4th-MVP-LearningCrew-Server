package com.depth.learningcrew.domain.studygroup.repository;

import com.depth.learningcrew.domain.studygroup.entity.Dibs;
import com.depth.learningcrew.domain.studygroup.entity.DibsId;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DibsRepository extends JpaRepository<Dibs, DibsId> {

    boolean existsByIdUserAndIdStudyGroup(User user, StudyGroup studyGroup);
}
