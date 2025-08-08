package com.depth.learningcrew.domain.note.repository;

import com.depth.learningcrew.domain.note.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
}
