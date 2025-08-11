package com.depth.learningcrew.domain.quiz.repository;

import static com.depth.learningcrew.domain.quiz.entity.QQuizOption.quizOption;

import com.depth.learningcrew.domain.quiz.entity.QQuiz;
import com.depth.learningcrew.domain.quiz.entity.Quiz;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class QuizQueryRepository {
    private final JPAQueryFactory queryFactory;

    /**
     * 스터디 그룹의 특정 스텝에 해당하는 모든 퀴즈(선택지 포함)를 조회합니다.
     *
     * @param studyGroup 스터디 그룹 (필수)
     * @param step         스텝 번호 (필수)
     * @return 퀴즈 엔티티 리스트(quizOptions fetch-join 로딩 완료)
     */
    public List<Quiz> findAllOfStepWithOptions(StudyGroup studyGroup, Integer step) {

        QQuiz q = new QQuiz("q");

        var query = queryFactory
                .selectFrom(q)
                .leftJoin(q.quizOptions, quizOption).fetchJoin()
                .where(
                        q.studyGroup.eq(studyGroup),
                        q.step.eq(step)
                );

        applyDefaultSorting(query);

        List<Quiz> rows = query.fetch();

        Map<Long, Quiz> dedup = new LinkedHashMap<>();
        for (Quiz quiz : rows) {
            dedup.putIfAbsent(quiz.getId(), quiz);
        }
        return List.copyOf(dedup.values());
    }

    private void applyDefaultSorting(JPAQuery<Quiz> query) {
        QQuiz q = new QQuiz("q");

        query.orderBy(
                q.id.asc(),
                quizOption.id.optionNum.asc()
        );
    }
}
