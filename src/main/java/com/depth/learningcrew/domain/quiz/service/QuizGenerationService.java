package com.depth.learningcrew.domain.quiz.service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.depth.learningcrew.domain.ai.llm.dto.OptionsPayload;
import com.depth.learningcrew.domain.ai.llm.dto.QuizzesPayload;
import com.depth.learningcrew.domain.ai.llm.service.OptionGenerator;
import com.depth.learningcrew.domain.ai.llm.service.QuizGenerator;
import com.depth.learningcrew.domain.note.entity.Note;
import com.depth.learningcrew.domain.note.repository.NoteRepository;
import com.depth.learningcrew.domain.quiz.repository.QuizRepository;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.system.limiter.llm.TpmRateLimiter;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizGenerationService {

    private final StudyGroupRepository studyGroupRepository;
    private final NoteRepository noteRepository;
    private final QuizRepository quizRepository;
    private final QuizGenerator quizGenerator;
    private final OptionGenerator optionGenerator;
    private final ObjectMapper objectMapper;
    private final TpmRateLimiter tpmRateLimiter;
    private final QuizPersistService persistService;
    private final com.depth.learningcrew.domain.studygroup.repository.StudyStepQueryRepository studyStepQueryRepository;

    private final Map<GenKey, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final Semaphore inFlightLimiter = new Semaphore(4, true);

    private static final int OPTION_MAX_LEN = 255;

    public void generateForAllEndedStepsWithoutQuizzes() {
        LocalDate today = LocalDate.now();
        var steps = studyStepQueryRepository.findEndedStepsWithoutQuizzes(today);
        for (var step : steps) {
            Long groupId = step.getId().getStudyGroupId().getId();
            Integer stepNum = step.getId().getStep();
            generateForGroupAndPrevStep(groupId, stepNum);
        }
    }

    public void generateForGroupAndPrevStep(Long studyGroupId, Integer step) {
        GenKey key = new GenKey(studyGroupId, step, LocalDate.now());
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();

        try {
            if (existsToday(studyGroupId, step)) {
                log.info("[quiz-gen] skip: already generated today gid={} step={}", studyGroupId, step);
                return;
            }

            StudyGroup group = studyGroupRepository.findById(studyGroupId)
                    .orElseThrow(() -> new IllegalArgumentException("StudyGroup not found: " + studyGroupId));

            List<Note> notes = noteRepository.findByStudyGroup_IdAndStep(studyGroupId, step);
            if (notes.isEmpty()) {
                log.info("[quiz-gen] skip: no notes gid={} step={}", studyGroupId, step);
                return;
            }

            String mergedNotes = mergeNotes(notes);

            // LLM 호출 1: 퀴즈 생성
            long quizTokensEst = estimateTokensForQuiz(mergedNotes);
            tpmRateLimiter.acquire(quizTokensEst);

            QuizzesPayload q = withPermitAndRetry(() -> quizGenerator.generate(group.getName(), step, mergedNotes));
            validateQuizzes(q);

            // LLM 호출 2: 보기 생성
            String itemsJson = toItemsJson(q);

            long optionTokensEst = estimateTokensForOptions(itemsJson);
            tpmRateLimiter.acquire(optionTokensEst);

            OptionsPayload opts = withPermitAndRetry(() -> {
                OptionsPayload tmp = optionGenerator.generate(itemsJson);
                validateOptions(q, tmp); // 구조 검증 + 정답 일치 + 길이 검증 수행.
                strictAnswerConsistencyCheck(q, tmp);
                validateOptionLengths(tmp); // 길이 위반 시 예외 → withPermitAndRetry가 재시도
                return tmp;
            });

            opts = shuffleAndReindex(q, opts); // 선지 순서를 서버에서 섞고 answerIndex를 다시 맞춘다

            // 저장 -> 트랜잭션 + DB 락 + 최종 중복 검사
            persistService.persistAtomic(group.getId(), step, q, opts);

            log.info("🟢[quiz-gen] success gid={} step={} estTokens(q+opt)={}", studyGroupId, step,
                    (quizTokensEst + optionTokensEst));
        } catch (Exception e) {
            log.error("🔴[quiz-gen] failed gid={} step={} err={}", studyGroupId, step, e.getMessage(), e);
        } finally {
            lock.unlock();
            if (!lock.hasQueuedThreads()) {
                locks.remove(key, lock);
            }
        }
    }

    // 레이트리밋 + 재시도 유틸
    private <T> T withPermitAndRetry(Supplier<T> supplier) {
        boolean acquired = false;
        try {
            inFlightLimiter.acquire();
            acquired = true;
            return retry(supplier, 3, 1_000L); // 3회, 1s→2s→4s 백오프
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ie);
        } finally {
            if (acquired)
                inFlightLimiter.release();
        }
    }

    private <T> T retry(Supplier<T> action, int maxAttempts, long initialDelayMs) {
        long delay = initialDelayMs;
        RuntimeException last = null;
        for (int i = 1; i <= maxAttempts; i++) {
            try {
                return action.get();
            } catch (RuntimeException ex) {
                last = ex;
                if (i < maxAttempts) {
                    sleep(delay);
                    delay = Math.min(delay * 2, 8000);
                }
            }
        }
        throw last != null ? last : new RuntimeException("retry failed");
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    // 토큰 추정 (간단 보수치)
    private long estimateTokensForQuiz(String mergedNotes) {
        long input = mergedNotes != null ? mergedNotes.length() : 0;
        long output = 20L * 150;
        return Math.round((input + output) * 1.2);
    }

    private long estimateTokensForOptions(String itemsJson) {
        long input = itemsJson != null ? itemsJson.length() : 0;
        long output = 20L * 120;
        return Math.round((input + output) * 1.2);
    }

    // 유틸/검증/저장
    private boolean existsToday(Long groupId, Integer step) {
        LocalDateTime s = LocalDate.now().atStartOfDay();
        LocalDateTime e = s.plusDays(1);
        return quizRepository.existsByStudyGroup_IdAndStepAndCreatedAtBetween(groupId, step, s, e);
    }

    // Group의 특정 Step에 속하는 모든 Note 통합
    private String mergeNotes(List<Note> notes) {
        StringBuilder sb = new StringBuilder(400_000);
        for (Note n : notes) {
            sb.append("## ");
            if (n.getTitle() != null)
                sb.append(n.getTitle());
            sb.append("\n");
            if (n.getContent() != null)
                sb.append(n.getContent());
            sb.append("\n\n");
        }
        return sb.toString();
    }

    private String toItemsJson(QuizzesPayload q) {
        try {
            List<Map<String, Object>> items = q.getQuizzes().stream()
                    .map(it -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", it.getId());
                        m.put("stem", it.getStem());
                        m.put("answer", it.getAnswer());
                        return m;
                    })
                    .toList();
            Map<String, Object> wrapper = Map.of("items", items);
            return objectMapper.writeValueAsString(wrapper);
        } catch (Exception e) {
            throw new RuntimeException("toItemsJson failed", e);
        }
    }

    private void validateQuizzes(QuizzesPayload q) {
        if (q == null || q.getQuizzes() == null || q.getQuizzes().size() != 20) {
            throw new IllegalStateException("Questions must be exactly 20");
        }
        long distinct = q.getQuizzes().stream().map(QuizzesPayload.Item::getId).distinct().count();
        if (distinct != 20)
            throw new IllegalStateException("Question IDs must be unique");
        boolean anyBlank = q.getQuizzes().stream()
                .anyMatch(it -> isBlank(it.getId()) || isBlank(it.getStem()) || isBlank(it.getAnswer()));
        if (anyBlank)
            throw new IllegalStateException("id/stem/answer must be non-empty");
    }

    private void validateOptions(QuizzesPayload q, OptionsPayload opts) {
        if (opts == null || opts.getOptions() == null || opts.getOptions().size() != 20) {
            throw new IllegalStateException("Options must be exactly 20");
        }
        Map<String, QuizzesPayload.Item> qMap = q.getQuizzes().stream()
                .collect(Collectors.toMap(QuizzesPayload.Item::getId, it -> it));
        for (OptionsPayload.Opt o : opts.getOptions()) {
            if (!qMap.containsKey(o.getId()))
                throw new IllegalStateException("Option id not found: " + o.getId());
            if (o.getChoices() == null || o.getChoices().size() != 4)
                throw new IllegalStateException("Choices must be 4");
            if (o.getAnswerIndex() == null || o.getAnswerIndex() < 0 || o.getAnswerIndex() > 3) {
                throw new IllegalStateException("answerIndex must be 0..3");
            }
            long dc = o.getChoices().stream().distinct().count();
            if (dc != 4)
                throw new IllegalStateException("Choices must be all distinct for id=" + o.getId());
        }
    }

    private void validateOptionLengths(OptionsPayload opts) {
        for (OptionsPayload.Opt o : opts.getOptions()) {
            for (String c : o.getChoices()) {
                if (c != null && c.length() > OPTION_MAX_LEN) {
                    throw new IllegalStateException("Choice exceeds 255 chars");
                }
            }
        }
    }

    private void strictAnswerConsistencyCheck(QuizzesPayload q, OptionsPayload opts) {
        Map<String, String> answerById = q.getQuizzes().stream()
                .collect(Collectors.toMap(QuizzesPayload.Item::getId, QuizzesPayload.Item::getAnswer));
        for (OptionsPayload.Opt o : opts.getOptions()) {
            String ans = normalize(answerById.get(o.getId()));
            String chosen = normalize(o.getChoices().get(o.getAnswerIndex()));
            if (!ans.equals(chosen)) {
                throw new IllegalStateException("answer/choices mismatch id=" + o.getId());
            }
        }
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private record GenKey(Long groupId, Integer step, LocalDate date) {
    }

    private OptionsPayload shuffleAndReindex(QuizzesPayload q, OptionsPayload opts) {
        Map<String, String> ansById = q.getQuizzes().stream()
                .collect(Collectors.toMap(QuizzesPayload.Item::getId, QuizzesPayload.Item::getAnswer));

        Random rnd;
        try {
            rnd = SecureRandom.getInstanceStrong();
        } catch (Exception ignore) {
            rnd = new SecureRandom();
        }

        List<OptionsPayload.Opt> shuffled = new ArrayList<>();
        for (OptionsPayload.Opt o : opts.getOptions()) {
            String canonical = normalize(ansById.get(o.getId()));

            List<String> src = new ArrayList<>(o.getChoices());
            Collections.shuffle(src, rnd);

            int newIdx = -1;
            for (int i = 0; i < src.size(); i++) {
                if (normalize(src.get(i)).equals(canonical)) {
                    newIdx = i;
                    break;
                }
            }
            if (newIdx < 0)
                throw new IllegalStateException("Correct choice not found after shuffle id=" + o.getId());

            OptionsPayload.Opt neo = new OptionsPayload.Opt();
            neo.setId(o.getId());
            neo.setChoices(src);
            neo.setAnswerIndex(newIdx);
            shuffled.add(neo);
        }
        OptionsPayload res = new OptionsPayload();
        res.setOptions(shuffled);
        return res;
    }
}
