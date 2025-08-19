package com.depth.learningcrew.domain.studygroup.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.depth.learningcrew.domain.quiz.entity.QQuiz;
import com.depth.learningcrew.domain.studygroup.entity.QStudyStep;
import com.depth.learningcrew.domain.studygroup.entity.StudyStep;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StudyStepQueryRepository {

  private final JPAQueryFactory queryFactory;

  /**
   * 이미 종료일이 지난 스텝들 중, 퀴즈가 단 하나도 생성되지 않은 스텝을 모두 조회합니다.
   *
   * @param today 기준 일자(미포함). ex) LocalDate.now()
   */
  public List<StudyStep> findEndedStepsWithoutQuizzes(LocalDate today) {
    QStudyStep s = new QStudyStep("s");
    QQuiz q = new QQuiz("q");

    return queryFactory
        .selectFrom(s)
        .where(
            s.endDate.before(today),
            JPAExpressions.selectOne()
                .from(q)
                .where(
                    q.studyGroup.eq(s.id.studyGroupId),
                    q.step.eq(s.id.step))
                .notExists())
        .fetch();
  }
}
