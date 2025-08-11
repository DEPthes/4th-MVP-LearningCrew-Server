package com.depth.learningcrew.domain.quiz.controller;

import com.depth.learningcrew.domain.quiz.dto.QuizDto;
import com.depth.learningcrew.domain.quiz.service.QuizService;
import com.depth.learningcrew.system.security.model.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-groups/{studyGroupId}/steps")
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/{step}/quiz")
    public List<QuizDto.QuizResponse> getStepQuizzes(
            @PathVariable Long studyGroupId,
            @PathVariable Integer step,
            @AuthenticationPrincipal UserDetails userDetails) {
        return quizService.getStepQuizzes(studyGroupId, step, userDetails);
    }
}
