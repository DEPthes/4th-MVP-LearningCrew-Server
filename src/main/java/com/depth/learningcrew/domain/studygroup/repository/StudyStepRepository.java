package com.depth.learningcrew.domain.studygroup.repository;

import com.depth.learningcrew.domain.studygroup.entity.StudyStep;
import com.depth.learningcrew.domain.studygroup.entity.StudyStepId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyStepRepository extends JpaRepository<StudyStep, StudyStepId> {
}