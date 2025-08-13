package com.depth.learningcrew.domain.quiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.depth.learningcrew.domain.quiz.dto.QuizRecordDto;
import com.depth.learningcrew.domain.quiz.repository.QuizRecordQueryRepository;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

@ExtendWith(MockitoExtension.class)
class QuizRecordServiceTest {

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private MemberQueryRepository memberQueryRepository;

    @Mock
    private QuizRecordQueryRepository quizRecordQueryRepository;

    @InjectMocks
    private QuizService quizRecordService;

    private User testUser;
    private UserDetails testUserDetails;
    private StudyGroup testStudyGroup;
    private QuizRecordDto.SearchConditions searchConditions;
    private Pageable pageable;

    private final ObjectMapper om = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("tester")
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        testUserDetails = UserDetails.builder()
                .user(testUser)
                .build();

        testStudyGroup = StudyGroup.builder()
                .id(100L)
                .name("테스트 그룹")
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        searchConditions = QuizRecordDto.SearchConditions.builder()
                .sort("step")
                .order("asc")
                .build();

        pageable = PageRequest.of(0, 10);
    }

    private QuizRecordDto.QuizRecordResponse row(long userId, int step, int total, int correct) {
        int score = total == 0 ? 0 : Math.round((float) correct * 100 / total);
        return QuizRecordDto.QuizRecordResponse.builder()
                .userId(userId)
                .step(step)
                .totalQuizCount(total)
                .correctCount(correct)
                .score(score)
                .build();
    }

    private void printJson(String label, Object obj) {
        try {
            System.out.println("==== " + label + " (JSON) ====");
            System.out.println(om.writeValueAsString(obj));
            System.out.println("=============================");
        } catch (Exception e) {
            System.out.println("JSON 출력 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("퀴즈 제출 기록 페이지 조회 - 성공(기본 정렬 step asc)")
    void paginateQuizRecords_ShouldReturnPagedResults() {
        // given
        Page<QuizRecordDto.QuizRecordResponse> mockPage = new PageImpl<>(
                List.of(
                        row(1L, 1, 5, 4), // 80
                        row(1L, 2, 4, 4)  // 100
                ),
                pageable,
                2L
        );

        when(studyGroupRepository.findById(100L)).thenReturn(Optional.of(testStudyGroup));
        when(memberQueryRepository.isMember(testStudyGroup, testUser)).thenReturn(true);
        when(quizRecordQueryRepository.paginateQuizRecords(
                eq(testStudyGroup), eq(testUserDetails), eq(searchConditions), eq(pageable)))
                .thenReturn(mockPage);

        // when
        PagedModel<QuizRecordDto.QuizRecordResponse> result = quizRecordService.paginateQuizRecords(
                100L, searchConditions, testUserDetails, pageable);

        // then
        verify(studyGroupRepository, times(1)).findById(100L);
        verify(memberQueryRepository, times(1)).isMember(testStudyGroup, testUser);
        verify(quizRecordQueryRepository, times(1))
                .paginateQuizRecords(eq(testStudyGroup), eq(testUserDetails), eq(searchConditions), eq(pageable));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getStep()).isEqualTo(1);
        assertThat(result.getContent().get(1).getScore()).isEqualTo(100);

        printJson("성공#1 기본정렬", result);
        boolean ok = result.getContent().size() == 2 && result.getContent().get(1).getScore() == 100;
        System.out.println("[성공#1] 테스트 성공 여부 = " + ok);
        assertThat(ok).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 스터디 그룹이면 404를 던진다")
    void paginateQuizRecords_WithNonExistentGroup_ShouldThrowNotFound() {
        // given
        when(studyGroupRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                quizRecordService.paginateQuizRecords(999L, searchConditions, testUserDetails, pageable)
        ).isInstanceOf(RestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_GROUP_NOT_FOUND);

        System.out.println("[404] STUDY_GROUP_NOT_FOUND 발생 (정상 동작)");
    }

    @Test
    @DisplayName("멤버가 아닌 사용자가 조회하면 403을 던진다")
    void paginateQuizRecords_WithNonMember_ShouldThrowForbidden() {
        // given
        when(studyGroupRepository.findById(100L)).thenReturn(Optional.of(testStudyGroup));
        when(memberQueryRepository.isMember(testStudyGroup, testUser)).thenReturn(false);

        // when & then
        assertThatThrownBy(() ->
                quizRecordService.paginateQuizRecords(100L, searchConditions, testUserDetails, pageable)
        ).isInstanceOf(RestException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_GROUP_NOT_MEMBER);

        System.out.println("[403] STUDY_GROUP_NOT_MEMBER 발생 (정상 동작)");
    }

    @Test
    @DisplayName("빈 결과여도 정상적으로 PagedModel을 반환한다")
    void paginateQuizRecords_WithEmptyResults_ShouldReturnEmptyPagedModel() {
        // given
        Page<QuizRecordDto.QuizRecordResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0L);

        when(studyGroupRepository.findById(100L)).thenReturn(Optional.of(testStudyGroup));
        when(memberQueryRepository.isMember(testStudyGroup, testUser)).thenReturn(true);
        when(quizRecordQueryRepository.paginateQuizRecords(
                eq(testStudyGroup), eq(testUserDetails), eq(searchConditions), eq(pageable)))
                .thenReturn(emptyPage);

        // when
        PagedModel<QuizRecordDto.QuizRecordResponse> result = quizRecordService.paginateQuizRecords(
                100L, searchConditions, testUserDetails, pageable);

        // then
        verify(studyGroupRepository, times(1)).findById(100L);
        verify(memberQueryRepository, times(1)).isMember(testStudyGroup, testUser);
        verify(quizRecordQueryRepository, times(1))
                .paginateQuizRecords(eq(testStudyGroup), eq(testUserDetails), eq(searchConditions), eq(pageable));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();

        printJson("빈 결과", result);
        boolean ok = result.getContent().isEmpty();
        System.out.println("[빈 결과] 테스트 성공 여부 = " + ok);
        assertThat(ok).isTrue();
    }

    @Test
    @DisplayName("정렬/필터 조건(created_at desc, step=2)을 적용하여 조회할 수 있다")
    void paginateQuizRecords_WithDifferentSortConditions_ShouldWorkCorrectly() {
        // given
        QuizRecordDto.SearchConditions cond = QuizRecordDto.SearchConditions.builder()
                .sort("created_at")
                .order("desc")
                .build();

        Page<QuizRecordDto.QuizRecordResponse> mockPage = new PageImpl<>(
                List.of(row(1L, 2, 3, 2)), // 67
                pageable,
                1L
        );

        when(studyGroupRepository.findById(100L)).thenReturn(Optional.of(testStudyGroup));
        when(memberQueryRepository.isMember(testStudyGroup, testUser)).thenReturn(true);
        when(quizRecordQueryRepository.paginateQuizRecords(
                eq(testStudyGroup), eq(testUserDetails), eq(cond), eq(pageable)))
                .thenReturn(mockPage);

        // when
        PagedModel<QuizRecordDto.QuizRecordResponse> result = quizRecordService.paginateQuizRecords(
                100L, cond, testUserDetails, pageable);

        // then
        verify(quizRecordQueryRepository, times(1))
                .paginateQuizRecords(eq(testStudyGroup), eq(testUserDetails), eq(cond), eq(pageable));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStep()).isEqualTo(2);
        assertThat(result.getContent().get(0).getScore()).isEqualTo(67);

        printJson("성공#2 step=2 + created_at desc", result);
        boolean ok = result.getContent().size() == 1 && result.getContent().get(0).getStep() == 2;
        System.out.println("[성공#2] 테스트 성공 여부 = " + ok);
        assertThat(ok).isTrue();
    }

    @Test
    @DisplayName("다른 Pageable(page=1,size=5)로 조회할 수 있다")
    void paginateQuizRecords_WithDifferentPageable_ShouldWorkCorrectly() {
        // given
        Pageable custom = PageRequest.of(1, 5);
        Page<QuizRecordDto.QuizRecordResponse> mockPage = new PageImpl<>(
                List.of(row(1L, 3, 4, 3)), custom, 1L);

        when(studyGroupRepository.findById(100L)).thenReturn(Optional.of(testStudyGroup));
        when(memberQueryRepository.isMember(testStudyGroup, testUser)).thenReturn(true);
        when(quizRecordQueryRepository.paginateQuizRecords(
                eq(testStudyGroup), eq(testUserDetails), eq(searchConditions), eq(custom)))
                .thenReturn(mockPage);

        // when
        PagedModel<QuizRecordDto.QuizRecordResponse> result = quizRecordService.paginateQuizRecords(
                100L, searchConditions, testUserDetails, custom);

        // then
        verify(quizRecordQueryRepository, times(1))
                .paginateQuizRecords(eq(testStudyGroup), eq(testUserDetails), eq(searchConditions), eq(custom));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStep()).isEqualTo(3);
        assertThat(result.getContent().get(0).getScore()).isEqualTo(75);

        printJson("다른 Pageable(page=1,size=5)", result);
        boolean ok = result.getContent().size() == 1 && result.getContent().get(0).getStep() == 3;
        System.out.println("[Pageable] 테스트 성공 여부 = " + ok);
        assertThat(ok).isTrue();
    }

    @Test
    @DisplayName("여러 스텝 기록이 있을 때 모두 반환한다")
    void paginateQuizRecords_WithMultipleResults_ShouldReturnAllResults() {
        // given
        Page<QuizRecordDto.QuizRecordResponse> mockPage = new PageImpl<>(
                List.of(
                        row(1L, 1, 5, 3), // 60
                        row(1L, 2, 4, 2), // 50
                        row(1L, 3, 2, 2)  // 100
                ),
                pageable,
                3L
        );

        when(studyGroupRepository.findById(100L)).thenReturn(Optional.of(testStudyGroup));
        when(memberQueryRepository.isMember(testStudyGroup, testUser)).thenReturn(true);
        when(quizRecordQueryRepository.paginateQuizRecords(
                eq(testStudyGroup), eq(testUserDetails), eq(searchConditions), eq(pageable)))
                .thenReturn(mockPage);

        // when
        PagedModel<QuizRecordDto.QuizRecordResponse> result = quizRecordService.paginateQuizRecords(
                100L, searchConditions, testUserDetails, pageable);

        // then
        verify(quizRecordQueryRepository, times(1))
                .paginateQuizRecords(eq(testStudyGroup), eq(testUserDetails), eq(searchConditions), eq(pageable));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).getScore()).isEqualTo(60);
        assertThat(result.getContent().get(2).getScore()).isEqualTo(100);

        printJson("다건 결과", result);
        boolean ok = result.getContent().size() == 3
                && result.getContent().get(0).getScore() == 60
                && result.getContent().get(2).getScore() == 100;
        System.out.println("[다건 결과] 테스트 성공 여부 = " + ok);
        assertThat(ok).isTrue();
    }

    @Test
    @DisplayName("null 검색 조건이 주어져도 기본값으로 처리된다")
    void paginateQuizRecords_WithNullSearchConditions_ShouldUseDefaultValues() {
        // given
        QuizRecordDto.SearchConditions nullConditions = new QuizRecordDto.SearchConditions();
        Page<QuizRecordDto.QuizRecordResponse> mockPage =
                new PageImpl<>(List.of(row(1L, 1, 5, 4)), pageable, 1L);

        when(studyGroupRepository.findById(100L)).thenReturn(Optional.of(testStudyGroup));
        when(memberQueryRepository.isMember(testStudyGroup, testUser)).thenReturn(true);
        when(quizRecordQueryRepository.paginateQuizRecords(
                eq(testStudyGroup), eq(testUserDetails), eq(nullConditions), eq(pageable)))
                .thenReturn(mockPage);

        // when
        PagedModel<QuizRecordDto.QuizRecordResponse> result = quizRecordService.paginateQuizRecords(
                100L, nullConditions, testUserDetails, pageable);

        // then
        verify(quizRecordQueryRepository, times(1))
                .paginateQuizRecords(eq(testStudyGroup), eq(testUserDetails), eq(nullConditions), eq(pageable));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getScore()).isEqualTo(80);

        printJson("null 검색조건(기본값 적용)", result);
        boolean ok = result.getContent().size() == 1 && result.getContent().get(0).getScore() == 80;
        System.out.println("[null 조건] 테스트 성공 여부 = " + ok);
        assertThat(ok).isTrue();
    }
}
