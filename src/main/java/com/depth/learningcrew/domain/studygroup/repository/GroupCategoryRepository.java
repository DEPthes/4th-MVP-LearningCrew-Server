package com.depth.learningcrew.domain.studygroup.repository;

import com.depth.learningcrew.domain.studygroup.entity.GroupCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupCategoryRepository extends JpaRepository<GroupCategory, Integer> {
}
