package com.depth.learningcrew.domain.quiz.repository;

import static com.depth.learningcrew.domain.quiz.entity.QQuiz.quiz1;
import static com.depth.learningcrew.domain.quiz.entity.QQuizRecord.quizRecord;

import com.depth.learningcrew.domain.quiz.dto.QuizRecordDto;
import com.depth.learningcrew.domain.quiz.entity.QuizRecord;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.security.model.UserDetails;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class QuizRecordQueryRepository {
    private final JPAQueryFactory queryFactory;

    public boolean existsUserSubmittedStep(StudyGroup studyGroup, User user, Integer step) {

        Integer fetched = queryFactory
                .selectOne()
                .from(quizRecord)
                .join(quizRecord.id.quiz, quiz1)
                .where(
                        quiz1.studyGroup.eq(studyGroup),
                        quizRecord.id.user.eq(user),
                        quiz1.step.eq(step)
                )
                .fetchFirst();

        return fetched != null;
    }

    public Page<QuizRecordDto.QuizRecordResponse> paginateQuizRecords(
            StudyGroup studyGroup,
            UserDetails user,
            QuizRecordDto.SearchConditions searchConditions,
            Pageable pageable
    ) {
        JPAQuery<Tuple> contentQuery = buildBaseQuery(studyGroup, user, searchConditions);
        applySorting(contentQuery, searchConditions);

        List<Tuple> results = contentQuery
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = buildCountQuery(studyGroup, user)
                .fetchOne();

        List<QuizRecordDto.QuizRecordResponse> content = results.stream()
                .map(t -> mapToDto(t, user.getUser()))
                .toList();

        return new PageImpl<>(content, pageable, totalCount != null ? totalCount : 0L);
    }

    private JPAQuery<Tuple> buildBaseQuery(
            StudyGroup studyGroup,
            UserDetails user,
            QuizRecordDto.SearchConditions searchConditions
    ) {
        NumberExpression<Long> totalExpr = quiz1.id.countDistinct();
        NumberExpression<Integer> sumCorrectExpr = quizRecord.correctCount.sum();
        DateTimeExpression<LocalDateTime> maxCreatedExpr = quiz1.createdAt.max();

        return queryFactory
                .select(quiz1.step, totalExpr, sumCorrectExpr, maxCreatedExpr)
                .from(quizRecord)
                .join(quizRecord.id.quiz, quiz1)
                .where(
                        quiz1.studyGroup.eq(studyGroup),
                        quizRecord.id.user.eq(user.getUser())
                )
                .groupBy(quiz1.step);
    }

    private JPAQuery<Long> buildCountQuery(
            StudyGroup studyGroup,
            UserDetails user
    ) {
        return queryFactory
                .select(quiz1.step.countDistinct())
                .from(quizRecord)
                .join(quizRecord.id.quiz, quiz1)
                .where(
                        quiz1.studyGroup.eq(studyGroup),
                        quizRecord.id.user.eq(user.getUser())
                );
    }

    private void applySorting(JPAQuery<Tuple> query, QuizRecordDto.SearchConditions searchConditions) {
        String sort = searchConditions.getSort() != null ? searchConditions.getSort() : "step";
        String order = searchConditions.getOrder() != null ? searchConditions.getOrder() : "asc";

        if("step".equals(sort)) {
            if("asc".equals(order)) {
                query.orderBy(quiz1.studyGroup.steps.size().asc());
            } else {
                query.orderBy(quiz1.studyGroup.steps.size().desc());
            }
        } else {
            if("asc".equals(order)) {
                query.orderBy(quiz1.createdAt.asc());
            } else {
                query.orderBy(quiz1.createdAt.desc());
            }
        }
    }

    private QuizRecordDto.QuizRecordResponse mapToDto(Tuple tuple, User user) {
        Integer stepVal = tuple.get(quiz1.step);
        Long totalL = tuple.get(1, Long.class);
        Integer correctI = tuple.get(2, Integer.class);

        int total = totalL != null ? totalL.intValue() : 0;
        int correct = correctI != null ? correctI : 0;

        QuizRecord aggregated = QuizRecord.builder()
                .id(null)
                .correctCount(correct)
                .build();

        StudyGroup stepOnlyGroup = new StudyGroup();
        stepOnlyGroup.setCurrentStep(stepVal);

        return QuizRecordDto.QuizRecordResponse.from(user, stepOnlyGroup, total, aggregated);
    }
}
