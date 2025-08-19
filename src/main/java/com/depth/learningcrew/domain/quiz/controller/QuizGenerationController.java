package com.depth.learningcrew.domain.quiz.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.depth.learningcrew.domain.quiz.schedule.QuizScheduler;
import com.depth.learningcrew.domain.quiz.service.QuizGenerationService;
import com.depth.learningcrew.system.security.annotation.NoJwtAuth;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz")
@Tag(name = "Quiz Generation", description = "퀴즈 생성 API")
public class QuizGenerationController {

    private final QuizScheduler quizScheduler;

    /**
     * 스케줄러와 동일한 스캔 로직을 즉시(비동기) 실행한다.
     * - 스케줄러의 inProgress 키로 중복 제출은 자동 방지된다.
     * - 오래 걸릴 수 있으므로 202 Accepted로 즉시 반환한다.
     */
    @NoJwtAuth("Admin 레벨에서 다루는거라 인증 제외")
    @PostMapping("/admin/run/scan")
    public ResponseEntity<TriggerResponse> runScan() {
        String jobId = UUID.randomUUID().toString();
        quizScheduler.generateQuiz(); // 비동기 제출
        return ResponseEntity.accepted().body(new TriggerResponse("SUBMITTED", jobId));
    }

    public record TriggerResponse(String status, String jobId) {}

    private final QuizGenerationService quizGenerationService;

    @NoJwtAuth("Admin 레벨에서 다루는거라 인증 제외")
    @PostMapping("/admin/run/target")
    public ResponseEntity<?> runTarget(@RequestBody RunTargetRequest req) {
        quizGenerationService.generateForGroupAndPrevStep(req.groupId(), req.stepNum());
        return ResponseEntity.accepted().build();
    }

    public record RunTargetRequest(@NotNull Long groupId, @NotNull Integer stepNum) {}

    @NoJwtAuth("Admin 레벨에서 다루는거라 인증 제외")
    @PostMapping("/admin/run/backfill-missing")
    public ResponseEntity<?> runBackfillMissing() {
        quizGenerationService.generateForAllEndedStepsWithoutQuizzes();
        return ResponseEntity.accepted().build();
    }
}
