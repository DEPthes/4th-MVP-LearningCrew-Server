package com.depth.learningcrew.domain.note.repository;

import com.depth.learningcrew.domain.note.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByStudyGroup_IdAndStep(Long studyGroupId, Integer step);

    boolean existsByStudyGroup_IdAndStepAndCreatedBy_Id(Long studyGroupId, Integer step, Long userId);

    Optional<Note> findByStudyGroup_IdAndStepAndCreatedBy_Id(Long studyGroupId, Integer step, Long userId);
}
