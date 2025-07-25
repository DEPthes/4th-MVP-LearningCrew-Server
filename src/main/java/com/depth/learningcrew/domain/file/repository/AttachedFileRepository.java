package com.depth.learningcrew.domain.file.repository;

import com.depth.learningcrew.domain.file.entity.AttachedFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachedFileRepository extends JpaRepository<AttachedFile, String> {
}
