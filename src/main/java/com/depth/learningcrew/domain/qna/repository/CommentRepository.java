package com.depth.learningcrew.domain.qna.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.depth.learningcrew.domain.qna.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
