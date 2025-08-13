package com.depth.learningcrew.domain.quiz.service;

import com.depth.learningcrew.domain.quiz.dto.QuizRecordDto;
import com.depth.learningcrew.domain.quiz.entity.*;
import com.depth.learningcrew.domain.quiz.repository.QuizQueryRepository;
import com.depth.learningcrew.domain.quiz.repository.QuizRecordQueryRepository;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.MemberQueryRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 어디서 작성하나? -> src/test/java 하위에 JUnit 테스트 파일로 작성/실행하세요.
 * 바꿔도 되는 변수/메서드 이름: 주석으로 한글 표시했습니다.
 */
class QuizSubmissionServiceTest {

    // ----- 의존성 목킹 -----
    StudyGroupRepository studyGroupRepository = mock(StudyGroupRepository.class);
    MemberQueryRepository memberQueryRepository = mock(MemberQueryRepository.class);
    QuizQueryRepository quizQueryRepository = mock(QuizQueryRepository.class);
    QuizRecordQueryRepository quizRecordQueryRepository = mock(QuizRecordQueryRepository.class);

    // SUT (테스트 대상 서비스) - 생성자 시그니처는 실제 클래스에 맞춰주세요.
    QuizService sut = new QuizService(
            studyGroupRepository,
            memberQueryRepository,
            quizQueryRepository,
            quizRecordQueryRepository
    );

    private final ObjectMapper om = new ObjectMapper();

    // ----- 공통 더미 빌더 -----
    private StudyGroup buildStudyGroup(Integer currentStep) {
        StudyGroup sg = new StudyGroup();
        // 프로젝트에서 현재 스텝 필드가 다르면 이 부분만 바꾸세요. (예: currentStep)
        try {
            var f = StudyGroup.class.getDeclaredField("step");
            f.setAccessible(true);
            f.set(sg, currentStep);
        } catch (Exception ignore) {}
        return sg;
    }

    private User buildUser(Long id) {
        return User.builder().id(id).build();
    }

    private Quiz buildSingleAnswerQuiz(long id, int step, StudyGroup sg, int answerNum, int options) {
        Quiz q = Quiz.builder().id(id).step(step).studyGroup(sg).build();
        for (int i = 1; i <= options; i++) {
            q.getQuizOptions().add(
                    QuizOption.builder()
                            .id(QuizOptionId.of(q, i))
                            .content("옵션" + i)
                            .isAnswer(i == answerNum)
                            .build()
            );
        }
        return q;
    }

    private Quiz buildMultiAnswerQuiz(long id, int step, StudyGroup sg, List<Integer> answerNums, int options) {
        Quiz q = Quiz.builder().id(id).step(step).studyGroup(sg).build();
        for (int i = 1; i <= options; i++) {
            q.getQuizOptions().add(
                    QuizOption.builder()
                            .id(QuizOptionId.of(q, i))
                            .content("옵션" + i)
                            .isAnswer(answerNums.contains(i))
                            .build()
            );
        }
        return q;
    }

    private void printSuccess(String label, QuizRecordDto.QuizSubmitResponse resp) throws Exception {
        System.out.println("[" + label + "] 응답 JSON = " + om.writeValueAsString(resp));
        System.out.println("[" + label + "] 총문항/정답/점수 = "
                + resp.getTotalQuizCount() + "/" + resp.getCorrectCount() + "/" + resp.getScore());
    }

    // ========== 성공 #1: 전부 정답 (단일 + 복수) ==========
    @Test
    @DisplayName("submitStepAnswers: 성공 - 전부 정답")
    void submit_success_all_correct() throws Exception {
        Long studyGroupId = 1L;
        Integer step = 1;

        StudyGroup sg = buildStudyGroup(step);
        when(studyGroupRepository.findById(studyGroupId)).thenReturn(Optional.of(sg));

        User user = buildUser(10L);
        UserDetails ud = new UserDetails(user);

        when(memberQueryRepository.isMember(sg, user)).thenReturn(true);
        when(quizRecordQueryRepository.existsUserSubmittedStep(sg, user, step)).thenReturn(false);

        Quiz q1 = buildSingleAnswerQuiz(100L, step, sg, 2, 3);
        Quiz q2 = buildMultiAnswerQuiz(200L, step, sg, List.of(1, 3), 3);
        when(quizQueryRepository.findAllOfStepWithOptions(sg, step)).thenReturn(List.of(q1, q2));

        QuizRecordDto.QuizSubmitRequest req = QuizRecordDto.QuizSubmitRequest.builder()
                .answers(List.of(
                        QuizRecordDto.QuizSubmitRequest.Answer.builder().quizId(100L).selectedOptions(List.of(2)).build(),
                        QuizRecordDto.QuizSubmitRequest.Answer.builder().quizId(200L).selectedOptions(List.of(1, 3)).build()
                ))
                .build();

        QuizRecordDto.QuizSubmitResponse resp = sut.submitStepAnswers(studyGroupId, step, req, ud);

        printSuccess("성공#1(전부정답)", resp);
        assertThat(resp.getUserId()).isEqualTo(10L);
        assertThat(resp.getTotalQuizCount()).isEqualTo(2);
        assertThat(resp.getCorrectCount()).isEqualTo(2);
        assertThat(resp.getScore()).isEqualTo(100);
    }

    // ========== 성공 #2: 일부만 정답(혼합 채점) ==========
    @Test
    @DisplayName("submitStepAnswers: 성공 - 일부만 정답(혼합 채점)")
    void submit_success_partial_correct() throws Exception {
        Long studyGroupId = 1L;
        Integer step = 1;

        StudyGroup sg = buildStudyGroup(step);
        when(studyGroupRepository.findById(studyGroupId)).thenReturn(Optional.of(sg));

        User user = buildUser(11L);
        UserDetails ud = new UserDetails(user);

        when(memberQueryRepository.isMember(sg, user)).thenReturn(true);
        when(quizRecordQueryRepository.existsUserSubmittedStep(sg, user, step)).thenReturn(false);

        // q1(단일정답 2) -> 정답 제출
        // q2(복수정답 1,3) -> 일부만 선택(1만 선택) => 오답 처리
        Quiz q1 = buildSingleAnswerQuiz(101L, step, sg, 2, 4);
        Quiz q2 = buildMultiAnswerQuiz(201L, step, sg, List.of(1, 3), 4);
        when(quizQueryRepository.findAllOfStepWithOptions(sg, step)).thenReturn(List.of(q1, q2));

        QuizRecordDto.QuizSubmitRequest req = QuizRecordDto.QuizSubmitRequest.builder()
                .answers(List.of(
                        QuizRecordDto.QuizSubmitRequest.Answer.builder().quizId(101L).selectedOptions(List.of(2)).build(),
                        QuizRecordDto.QuizSubmitRequest.Answer.builder().quizId(201L).selectedOptions(List.of(1)).build()
                ))
                .build();

        QuizRecordDto.QuizSubmitResponse resp = sut.submitStepAnswers(studyGroupId, step, req, ud);

        printSuccess("성공#2(부분정답)", resp);
        assertThat(resp.getUserId()).isEqualTo(11L);
        assertThat(resp.getTotalQuizCount()).isEqualTo(2);
        assertThat(resp.getCorrectCount()).isEqualTo(1);
        assertThat(resp.getScore()).isEqualTo(50);
    }

    // ========== 성공 #3: 순서 무관 + 중복 선택 허용 ==========
    @Test
    @DisplayName("submitStepAnswers: 성공 - 순서무관/중복선택 허용")
    void submit_success_order_invariant_and_duplicates() throws Exception {
        Long studyGroupId = 1L;
        Integer step = 2;

        StudyGroup sg = buildStudyGroup(step);
        when(studyGroupRepository.findById(studyGroupId)).thenReturn(Optional.of(sg));

        User user = buildUser(12L);
        UserDetails ud = new UserDetails(user);

        when(memberQueryRepository.isMember(sg, user)).thenReturn(true);
        when(quizRecordQueryRepository.existsUserSubmittedStep(sg, user, step)).thenReturn(false);

        // q1 복수정답 {1,3}; 제출은 [3,1,1,3] -> Set 비교로 정답
        Quiz q1 = buildMultiAnswerQuiz(300L, step, sg, List.of(1, 3), 4);
        when(quizQueryRepository.findAllOfStepWithOptions(sg, step)).thenReturn(List.of(q1));

        QuizRecordDto.QuizSubmitRequest req = QuizRecordDto.QuizSubmitRequest.builder()
                .answers(List.of(
                        QuizRecordDto.QuizSubmitRequest.Answer.builder().quizId(300L).selectedOptions(Arrays.asList(3, 1, 1, 3)).build()
                ))
                .build();

        QuizRecordDto.QuizSubmitResponse resp = sut.submitStepAnswers(studyGroupId, step, req, ud);

        printSuccess("성공#3(순서무관+중복허용)", resp);
        assertThat(resp.getUserId()).isEqualTo(12L);
        assertThat(resp.getTotalQuizCount()).isEqualTo(1);
        assertThat(resp.getCorrectCount()).isEqualTo(1);
        assertThat(resp.getScore()).isEqualTo(100);
    }

    // ========== 성공 #4: 희소 번호 옵션(1,3,5) ==========
    @Test
    @DisplayName("submitStepAnswers: 성공 - 희소 번호 옵션(1,3,5)")
    void submit_success_sparse_option_numbers() throws Exception {
        Long studyGroupId = 1L;
        Integer step = 3;

        StudyGroup sg = buildStudyGroup(step);
        when(studyGroupRepository.findById(studyGroupId)).thenReturn(Optional.of(sg));

        User user = buildUser(13L);
        UserDetails ud = new UserDetails(user);

        when(memberQueryRepository.isMember(sg, user)).thenReturn(true);
        when(quizRecordQueryRepository.existsUserSubmittedStep(sg, user, step)).thenReturn(false);

        // q1: 옵션 번호 1,3,5만 존재. 정답 {1,5}
        Quiz q1 = Quiz.builder().id(500L).step(step).studyGroup(sg).build();
        q1.getQuizOptions().addAll(List.of(
                QuizOption.builder().id(QuizOptionId.of(q1, 1)).content("옵션1").isAnswer(true).build(),
                QuizOption.builder().id(QuizOptionId.of(q1, 3)).content("옵션3").isAnswer(false).build(),
                QuizOption.builder().id(QuizOptionId.of(q1, 5)).content("옵션5").isAnswer(true).build()
        ));
        when(quizQueryRepository.findAllOfStepWithOptions(sg, step)).thenReturn(List.of(q1));

        // 제출: [5,1] (순서 뒤집힘)
        QuizRecordDto.QuizSubmitRequest req = QuizRecordDto.QuizSubmitRequest.builder()
                .answers(List.of(
                        QuizRecordDto.QuizSubmitRequest.Answer.builder().quizId(500L).selectedOptions(List.of(5, 1)).build()
                ))
                .build();

        QuizRecordDto.QuizSubmitResponse resp = sut.submitStepAnswers(studyGroupId, step, req, ud);

        printSuccess("성공#4(희소옵션)", resp);
        assertThat(resp.getUserId()).isEqualTo(13L);
        assertThat(resp.getTotalQuizCount()).isEqualTo(1);
        assertThat(resp.getCorrectCount()).isEqualTo(1);
        assertThat(resp.getScore()).isEqualTo(100);
    }

    // ========== 403 Forbidden ==========
    @Test
    @DisplayName("submitStepAnswers: 403 - 스터디 그룹 멤버가 아님")
    void submit_forbidden_when_not_member() {
        Long studyGroupId = 1L;
        Integer step = 1;

        StudyGroup sg = buildStudyGroup(step);
        when(studyGroupRepository.findById(studyGroupId)).thenReturn(Optional.of(sg));

        User user = buildUser(20L);
        UserDetails ud = new UserDetails(user);

        when(memberQueryRepository.isMember(sg, user)).thenReturn(false);

        QuizRecordDto.QuizSubmitRequest req = QuizRecordDto.QuizSubmitRequest.builder()
                .answers(List.of()) // 도달 전 예외 발생
                .build();

        RestException ex = assertThrows(RestException.class, () ->
                sut.submitStepAnswers(studyGroupId, step, req, ud)
        );

        System.out.println("[403 케이스] 에러 코드 = " + ex.getErrorCode());
        System.out.println("[403 케이스] 메시지 = " + ex.getMessage());
        System.out.println("[403 케이스] 테스트 성공 여부 = " + (ex.getErrorCode() == ErrorCode.STUDY_GROUP_NOT_MEMBER));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.STUDY_GROUP_NOT_MEMBER);
    }

    // ========== 409 Conflict ==========
    @Test
    @DisplayName("submitStepAnswers: 409 - 이미 해당 스텝 제출함")
    void submit_conflict_when_already_submitted() {
        Long studyGroupId = 1L;
        Integer step = 1;

        StudyGroup sg = buildStudyGroup(step);
        when(studyGroupRepository.findById(studyGroupId)).thenReturn(Optional.of(sg));

        User user = buildUser(30L);
        UserDetails ud = new UserDetails(user);

        when(memberQueryRepository.isMember(sg, user)).thenReturn(true);
        when(quizRecordQueryRepository.existsUserSubmittedStep(sg, user, step)).thenReturn(true);

        QuizRecordDto.QuizSubmitRequest req = QuizRecordDto.QuizSubmitRequest.builder()
                .answers(List.of()) // 도달 전 예외 발생
                .build();

        RestException ex = assertThrows(RestException.class, () ->
                sut.submitStepAnswers(studyGroupId, step, req, ud)
        );

        System.out.println("[409 케이스] 에러 코드 = " + ex.getErrorCode());
        System.out.println("[409 케이스] 메시지 = " + ex.getMessage());
        System.out.println("[409 케이스] 테스트 성공 여부 = " + (ex.getErrorCode() == ErrorCode.QUIZ_ALREADY_SUBMITTED_IN_STEP));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.QUIZ_ALREADY_SUBMITTED_IN_STEP);
    }
}
