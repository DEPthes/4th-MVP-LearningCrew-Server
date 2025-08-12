package com.depth.learningcrew.domain.qna.repository;

import com.depth.learningcrew.domain.qna.dto.QAndADto;
import com.depth.learningcrew.domain.qna.entity.QAndA;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.depth.learningcrew.domain.qna.entity.QComment.comment;
import static com.depth.learningcrew.domain.qna.entity.QQAndA.qAndA;

@Repository
@RequiredArgsConstructor
public class QAndAQueryRepository {
    private final JPAQueryFactory queryFactory;

    public Page<QAndADto.QAndAResponse> paginateByType(
            Long groupId,
            QAndADto.SearchConditions searchConditions,
            Pageable pageable
    ) {
        var contentQuery = buildBaseQuery(groupId, searchConditions);
        applySorting(contentQuery, searchConditions);

        List<QAndA> rows = contentQuery
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = buildCountQuery(groupId, searchConditions)
                .fetchOne();

        Map<Long, Integer> commentCountMap = batchCountComments(rows);

        List<QAndADto.QAndAResponse> content = rows.stream()
                .map(e -> QAndADto.QAndAResponse.from(
                        Objects.requireNonNull(e),
                        commentCountMap.getOrDefault(e.getId(), 0)
                ))
                .toList();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private JPAQuery<QAndA> buildBaseQuery(
            Long groupId,
            QAndADto.SearchConditions c
    ) {
        JPAQuery<QAndA> query = queryFactory
                .select(qAndA)
                .from(qAndA);

        applyFilterTypeCondition(query, groupId);
        applySearchCondition(query, c);
        applyStepCondition(query, c);

        return query;
    }

    private JPAQuery<Long> buildCountQuery(
            Long groupId,
            QAndADto.SearchConditions c
    ) {
        JPAQuery<Long> query = queryFactory
                .select(qAndA.count())
                .from(qAndA);

        applyFilterTypeCondition(query, groupId);
        applySearchCondition(query, c);
        applyStepCondition(query, c);

        return query;
    }

    private void applyFilterTypeCondition(
            JPAQuery<?> query,
            Long groupId
    ) {
        query.where(qAndA.studyGroup.id.eq(groupId));

        // 필요 시, FilterType Enum 정의하고 그에 따라 필터만 갈아끼울 수 있다.
        // 확장을 고려한 설계임.
    }

    private void applySearchCondition(JPAQuery<?> query, QAndADto.SearchConditions c) {
        Predicate p = buildSearchPredicate(c);
        if (p != null) query.where(p);
    }

    private Predicate buildSearchPredicate(QAndADto.SearchConditions c) {
        if (!StringUtils.hasText(c.getSearchKeyword())) return null;
        String kw = c.getSearchKeyword();

        BooleanExpression titlePred = qAndA.title.containsIgnoreCase(kw);

        BooleanTemplate contentPred = Expressions.booleanTemplate(
                "lower(cast({0} as char)) like concat('%', lower({1}), '%')",
                qAndA.content, kw
        );

        return titlePred.or(contentPred);
    }

    private void applyStepCondition(JPAQuery<?> query, QAndADto.SearchConditions c) {
        if (c.getStep() != null) query.where(qAndA.step.eq(c.getStep()));
    }

    private void applySorting(JPAQuery<QAndA> query, QAndADto.SearchConditions c) {
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

    private Map<Long, Integer> batchCountComments(List<QAndA> rows) {
        if (rows == null || rows.isEmpty()) return Collections.emptyMap();
        List<Long> ids = rows.stream().map(QAndA::getId).toList();

        List<Tuple> tuples = queryFactory
                .select(qAndA.id, comment.count())
                .from(comment)
                .where(comment.qAndA.id.in(ids))
                .groupBy(comment.qAndA.id)
                .fetch();

        return tuples.stream().collect(Collectors.toMap(
                t -> t.get(qAndA.id),
                t -> Objects.requireNonNull(t.get(comment.count())).intValue()
        ));
    }

    public Page<QAndADto.QAndAResponse> paginateByGroup(
            Long groupId,
            QAndADto.SearchConditions c,
            Pageable pageable
    ) {
        return paginateByType(groupId, c, pageable);
    }
}