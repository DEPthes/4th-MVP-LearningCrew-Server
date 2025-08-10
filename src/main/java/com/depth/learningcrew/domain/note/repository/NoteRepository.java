package com.depth.learningcrew.domain.note.repository;

import com.depth.learningcrew.domain.note.entity.Note;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByStudyGroup_IdAndStep(Long studyGroupId, Integer step);
}
