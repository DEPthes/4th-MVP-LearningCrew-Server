package com.depth.learningcrew.domain.studygroup.repository;

import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
}
