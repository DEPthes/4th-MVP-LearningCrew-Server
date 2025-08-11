package com.depth.learningcrew.domain.quiz.schedule;

import com.depth.learningcrew.domain.quiz.service.QuizGenerationService;
import com.depth.learningcrew.domain.studygroup.entity.StudyStep;
import com.depth.learningcrew.domain.studygroup.repository.StudyStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizScheduler {

    private final QuizGenerationService service;
    private final StudyStepRepository studyStepRepository;

    private final ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();

    private final Set<String> inProgress = ConcurrentHashMap.newKeySet();

    @Scheduled(cron = "0 1 0-1 * * *", zone = "Asia/Seoul")
    public void run() {
        generateQuiz();
    }

    // 스케줄에 종속적이지 않고 별도로 테스트도 가능하도록 이렇게 뺐음.
    public void generateQuiz(){
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        var steps = studyStepRepository.findByEndDate(yesterday); // 어제가 EndDate인 Step

        steps.forEach(step -> {
            Integer stepNum = step.getId().getStep();
            Long groupId = step.getId().getStudyGroupId().getId();
            String key = groupId + ":" + stepNum + ":" + today;

            if (!inProgress.add(key)) {
                log.debug("skip duplicate schedule submit {}", key);
                return;
            }
            pool.submit(() -> {
                try {
                    service.generateForGroupAndPrevStep(groupId, stepNum);
                } finally {
                    inProgress.remove(key);
                }
            });
        });
    }
}
