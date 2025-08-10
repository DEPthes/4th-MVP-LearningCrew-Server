package com.depth.learningcrew.domain.studygroup.repository;

import com.depth.learningcrew.domain.studygroup.entity.StudyStep;
import com.depth.learningcrew.domain.studygroup.entity.StudyStepId;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudyStepRepository extends JpaRepository<StudyStep, StudyStepId> {
    List<StudyStep> findByEndDate(LocalDate endDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("select s from StudyStep s where s.id = :id")
    Optional<StudyStep> findByIdForUpdate(@Param("id") StudyStepId id);
}