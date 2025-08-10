package com.depth.learningcrew.domain.quiz.service;

import com.depth.learningcrew.domain.ai.llm.dto.OptionsPayload;
import com.depth.learningcrew.domain.ai.llm.dto.QuizzesPayload;
import com.depth.learningcrew.domain.quiz.entity.Quiz;
import com.depth.learningcrew.domain.quiz.entity.QuizOption;
import com.depth.learningcrew.domain.quiz.entity.QuizOptionId;
import com.depth.learningcrew.domain.quiz.repository.QuizOptionRepository;
import com.depth.learningcrew.domain.quiz.repository.QuizRepository;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.entity.StudyStep;
import com.depth.learningcrew.domain.studygroup.entity.StudyStepId;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizPersistService {

    private final StudyStepRepository studyStepRepository;
    private final QuizRepository quizRepository;
    private final QuizOptionRepository quizOptionRepository;
    private final StudyGroupRepository studyGroupRepository;

    // Transaction Self Invocation 방지
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistAtomic(Long groupId, Integer step,
                                 QuizzesPayload q, OptionsPayload opts) {

        StudyGroup groupRef = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("StudyGroup not found: " + groupId));

        StudyStepId stepId = StudyStepId.of(step, groupRef);
        StudyStep stepRef = studyStepRepository.findByIdForUpdate(stepId)
                .orElseThrow(() -> new IllegalStateException("StudyStep not found: gid=" + groupId + ", step=" + step));

        LocalDateTime s = LocalDate.now().atStartOfDay();
        LocalDateTime e = s.plusDays(1);
        if (quizRepository.existsByStudyGroup_IdAndStepAndCreatedAtBetween(groupId, step, s, e)) {
            log.info("🟡[quiz-gen] skip at persist: already exists gid={} step={}", groupId, step);
            return;
        }

        Map<String, OptionsPayload.Opt> optMap = opts.getOptions().stream()
                .collect(Collectors.toMap(OptionsPayload.Opt::getId, it -> it));

        for (QuizzesPayload.Item item : q.getQuizzes()) {
            Quiz quiz = Quiz.builder()
                    .quiz(item.getStem())
                    .step(step)
                    .studyGroup(groupRef)
                    .studyStep(stepRef)
                    .build();
            quiz = quizRepository.save(quiz);

            OptionsPayload.Opt o = optMap.get(item.getId());
            List<String> cs = o.getChoices();

            for (int i = 0; i < 4; i++) {
                QuizOption option = QuizOption.builder()
                        .id(QuizOptionId.of(quiz, i + 1))
                        .content(cs.get(i))
                        .isAnswer(i == o.getAnswerIndex())
                        .build();
                quizOptionRepository.save(option);
            }
        }
    }
}
