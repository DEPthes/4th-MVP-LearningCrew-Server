package com.depth.learningcrew.domain.studygroup.repository;

import com.depth.learningcrew.domain.studygroup.entity.Dibs;
import com.depth.learningcrew.domain.studygroup.entity.DibsId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DibsRepository extends JpaRepository<Dibs, DibsId> {
}
