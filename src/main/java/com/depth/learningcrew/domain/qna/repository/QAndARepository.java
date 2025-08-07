package com.depth.learningcrew.domain.qna.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.depth.learningcrew.domain.qna.entity.QAndA;

public interface QAndARepository extends JpaRepository<QAndA, Long> {

}
