package com.depth.learningcrew.domain.qna.repository;

import com.depth.learningcrew.domain.qna.dto.CommentDto;
import com.depth.learningcrew.domain.qna.entity.Comment;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.depth.learningcrew.domain.qna.entity.QComment.comment;
import static com.depth.learningcrew.domain.qna.entity.QQAndA.qAndA;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepository{

    private final JPAQueryFactory queryFactory;

    public Page<CommentDto.CommentResponse> paginateByQna(
            Long studyGroupId, Long qnaId,
            CommentDto.SearchConditions searchConditions,
            Pageable pageable
    ) {
        JPAQuery<Comment> contentQuery = queryFactory
                .selectFrom(comment)
                .join(comment.qAndA, qAndA)
                .where(
                        qAndA.id.eq(qnaId),
                        qAndA.studyGroup.id.eq(studyGroupId)
                );

        applySorting(contentQuery, searchConditions);

        List<Comment> rows = contentQuery
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(comment.count())
                .from(comment)
                .join(comment.qAndA, qAndA)
                .where(
                        qAndA.id.eq(qnaId),
                        qAndA.studyGroup.id.eq(studyGroupId)
                )
                .fetchOne();

        List<CommentDto.CommentResponse> content = rows.stream()
                .map(CommentDto.CommentResponse::from)
                .toList();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private void applySorting(JPAQuery<Comment> query, CommentDto.SearchConditions c) {
        String sort = c.getSort();
        String order = c.getOrder();

        if ("alphabet".equals(sort)) {
            if ("asc".equals(order)) query.orderBy(qAndA.title.asc());
            else query.orderBy(qAndA.title.desc());
        } else { // created_at
            if ("asc".equals(order)) query.orderBy(qAndA.createdAt.asc());
            else query.orderBy(qAndA.createdAt.desc());
        }
    }
}