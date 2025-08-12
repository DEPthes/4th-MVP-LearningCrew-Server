package com.depth.learningcrew.domain.quiz.service;

import com.depth.learningcrew.domain.quiz.dto.QuizDto;
import com.depth.learningcrew.domain.quiz.entity.Quiz;
import com.depth.learningcrew.domain.quiz.entity.QuizOption;
import com.depth.learningcrew.domain.quiz.entity.QuizOptionId;
import com.depth.learningcrew.domain.quiz.repository.QuizQueryRepository;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.MemberQueryRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizQueryServiceTest {

    @Mock StudyGroupRepository studyGroupRepository;
    @Mock MemberQueryRepository memberQueryRepository;
    @Mock QuizQueryRepository quizQueryRepository;

    @InjectMocks QuizService quizService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    UserDetails userDetails;

    private static final ObjectMapper OM = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    @DisplayName("#1 멤버일 때: 퀴즈/옵션 DTO 반환 + '멤버입니다.' JSON 콘솔 출력")
    void getStepQuizzes_member_printJson() throws Exception {
        // --- given ---
        User user = new User(); setId(user, 10L);
        when(userDetails.getUser()).thenReturn(user);

        StudyGroup group = new StudyGroup(); setId(group, 1L);
        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(memberQueryRepository.isMember(group, user)).thenReturn(true);

        Quiz q = Quiz.builder().quiz("첫 번째 문제입니다.").step(1).build();
        setId(q, 123L); setCreatedAt(q, LocalDateTime.of(2025, 8, 1, 0, 0));
        q.addQuizOption(QuizOption.builder().id(QuizOptionId.of(q, 1)).content("선택지 1").isAnswer(false).build());
        q.addQuizOption(QuizOption.builder().id(QuizOptionId.of(q, 2)).content("선택지 2").isAnswer(true).build());

        when(quizQueryRepository.findAllOfStepWithOptions(group, 1)).thenReturn(List.of(q));

        // --- when ---
        List<QuizDto.QuizResponse> result = quizService.getStepQuizzes(1L, 1, userDetails);

        // --- then: 데이터 검증 ---
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(123L);
        assertThat(result.get(0).getOptions()).hasSize(2);

        // --- 콘솔 JSON 출력(성공: 멤버입니다.) ---
        Map<String, Object> success = new LinkedHashMap<>();
        success.put("status", "OK");
        success.put("message", "멤버입니다.");
        success.put("studyGroupId", 1L);
        success.put("userId", 10L);
        success.put("step", 1);
        success.put("quizCount", result.size());
        System.out.println("\n==== MEMBERSHIP RESULT (SUCCESS) ====\n" +
                OM.writerWithDefaultPrettyPrinter().writeValueAsString(success) + "\n");

        // (참고) 퀴즈 목록도 같이 보고 싶으면 주석 해제
        // System.out.println(OM.writerWithDefaultPrettyPrinter().writeValueAsString(result));
    }

    @Test
    @DisplayName("#N1 멤버가 아닐 때: 예외를 JSON 형태로 콘솔 출력")
    void getStepQuizzes_nonMember_printJsonError() throws Exception {
        // --- given ---
        User user = new User(); setId(user, 99L);
        when(userDetails.getUser()).thenReturn(user);

        StudyGroup group = new StudyGroup(); setId(group, 2L);
        when(studyGroupRepository.findById(2L)).thenReturn(Optional.of(group));
        when(memberQueryRepository.isMember(group, user)).thenReturn(false);

        // --- when ---
        RestException ex = catchThrowableOfType(
                () -> quizService.getStepQuizzes(2L, 3, userDetails),
                RestException.class
        );

        // --- then ---
        assertThat(ex).isNotNull();
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.STUDY_GROUP_NOT_MEMBER);

        // --- 콘솔 JSON 출력(에러) ---
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", "FORBIDDEN");
        error.put("code", ex.getErrorCode().name());
        // ErrorCode 메시지 접근 방식이 프로젝트마다 달라서, 우선 예외 메시지를 사용
        error.put("message", ex.getMessage());
        error.put("studyGroupId", 2L);
        error.put("userId", 99L);
        error.put("stepTried", 3);

        System.out.println("\n==== MEMBERSHIP RESULT (ERROR) ====\n" +
                OM.writerWithDefaultPrettyPrinter().writeValueAsString(error) + "\n");
    }

    // ===== 테스트 편의 리플렉션 유틸 =====
    private static void setId(Object target, Long id) throws Exception {
        Field f = null; Class<?> c = target.getClass();
        while (c != null) {
            try { f = c.getDeclaredField("id"); break; }
            catch (NoSuchFieldException ignored) { c = c.getSuperclass(); }
        }
        if (f == null) throw new NoSuchFieldException("id");
        f.setAccessible(true); f.set(target, id);
    }

    private static void setCreatedAt(Object entity, LocalDateTime t) throws Exception {
        Field f = entity.getClass().getSuperclass().getDeclaredField("createdAt");
        f.setAccessible(true); f.set(entity, t);
    }
}
