package com.depth.learningcrew.domain.quiz.repository;

import static com.depth.learningcrew.domain.quiz.entity.QQuizRecord.quizRecord;

import com.depth.learningcrew.domain.quiz.entity.QQuiz;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.user.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class QuizRecordQueryRepository {
    private final JPAQueryFactory queryFactory;

    public boolean existsUserSubmittedStep(StudyGroup studyGroup, User user, Integer step) {
        QQuiz q = new QQuiz("q");

        Integer fetched = queryFactory
                .selectOne()
                .from(quizRecord)
                .join(quizRecord.id.quiz, q)
                .where(
                        q.studyGroup.eq(studyGroup),
                        quizRecord.id.user.eq(user),
                        q.step.eq(step)
                )
                .fetchFirst();

        return fetched != null;
    }
}
