package com.depth.learningcrew.domain.quiz.controller;

import com.depth.learningcrew.domain.quiz.dto.QuizDto;
import com.depth.learningcrew.domain.quiz.dto.QuizRecordDto;
import com.depth.learningcrew.domain.quiz.service.QuizService;
import com.depth.learningcrew.system.security.model.UserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-groups/{studyGroupId}/steps")
@Tag(name = "Quiz", description = "퀴즈 API")
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/{step}/quiz")
    @Operation(summary = "스터디그룹의 스텝별 퀴즈 조회", description = "해당 스터디 그룹의 스텝별 퀴즈를 리스트 형태로 조회합니다.")
    public List<QuizDto.QuizResponse> getStepQuizzes(
            @PathVariable Long studyGroupId,
            @PathVariable Integer step,
            @AuthenticationPrincipal UserDetails userDetails) {
        return quizService.getStepQuizzes(studyGroupId, step, userDetails);
    }

    @PostMapping("/{step}/submit")
    @Operation(summary = "스텝별 퀴즈에 대한 답변 제출", description = "해당 스터기 그룹의 스텝별 퀴즈에 대한 답변을 제출합니다.")
    public QuizRecordDto.QuizSubmitResponse submitQuizInStep(
            @PathVariable Long studyGroupId,
            @PathVariable Integer step,
            @RequestBody QuizRecordDto.QuizSubmitRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return quizService.submitStepAnswers(studyGroupId, step, request, userDetails);
    }
}
