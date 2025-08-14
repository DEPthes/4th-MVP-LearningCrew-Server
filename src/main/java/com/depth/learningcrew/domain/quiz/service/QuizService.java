package com.depth.learningcrew.domain.quiz.service;

import com.depth.learningcrew.domain.quiz.dto.QuizDto;
import com.depth.learningcrew.domain.quiz.dto.QuizRecordDto;
import com.depth.learningcrew.domain.quiz.entity.Quiz;
import com.depth.learningcrew.domain.quiz.entity.QuizOption;
import com.depth.learningcrew.domain.quiz.entity.QuizRecord;
import com.depth.learningcrew.domain.quiz.entity.QuizRecordId;
import com.depth.learningcrew.domain.quiz.repository.QuizQueryRepository;
import com.depth.learningcrew.domain.quiz.repository.QuizRecordQueryRepository;
import com.depth.learningcrew.domain.quiz.repository.QuizRecordRepository;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.MemberQueryRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final StudyGroupRepository studyGroupRepository;
    private final MemberQueryRepository memberQueryRepository;
    private final QuizQueryRepository quizQueryRepository;
    private final QuizRecordQueryRepository quizRecordQueryRepository;
    private final QuizRecordRepository quizRecordRepository;

    @Transactional(readOnly = true)
    public List<QuizDto.QuizResponse> getStepQuizzes(
            Long studyGroupId,
            Integer step,
            UserDetails user) {

        StudyGroup studyGroup = studyGroupRepository.findById(studyGroupId)
                .orElseThrow(() -> new RestException(ErrorCode.STUDY_GROUP_NOT_FOUND));

        cannotViewIfNotMember(studyGroup, user);

        return quizQueryRepository.findAllOfStepWithOptions(studyGroup, step)
                .stream()
                .map(QuizDto.QuizResponse::from)
                .toList();
    }

    private void cannotViewIfNotMember(StudyGroup studyGroup, UserDetails user) {
        if(!memberQueryRepository.isMember(studyGroup, user.getUser())) {
            throw new RestException(ErrorCode.STUDY_GROUP_NOT_MEMBER);
        }
    }

    @Transactional
    public QuizRecordDto.QuizSubmitResponse submitStepAnswers(
            Long studyGroupId,
            Integer step,
            QuizRecordDto.QuizSubmitRequest request,
            UserDetails user) {

        StudyGroup studyGroup = studyGroupRepository.findById(studyGroupId)
                .orElseThrow(() -> new RestException(ErrorCode.STUDY_GROUP_NOT_FOUND));

        cannotWriteIfNotMember(studyGroup, user);
        cannotWriteWhenAlreadySubmitted(studyGroup, user, step);

        List<Quiz> quizzes = quizQueryRepository.findAllOfStepWithOptions(studyGroup, step);
        if(quizzes.isEmpty()) {
            throw new RestException(ErrorCode.QUIZ_NOT_FOUND);
        }

        Map<Long, QuizRecordDto.QuizSubmitRequest.Answer> answers = request.getAnswers().stream()
                .collect(Collectors.toMap(
                        QuizRecordDto.QuizSubmitRequest.Answer::getQuizId,
                        Function.identity()
                ));
        int correctCount = 0;
        List<QuizRecord> toSave = new ArrayList<>();

        for(Quiz quiz : quizzes) {
            QuizRecordDto.QuizSubmitRequest.Answer answer = answers.get(quiz.getId());
            if(answer == null) {
                throw new RestException(ErrorCode.GLOBAL_BAD_REQUEST);
            }

            Set<Integer> answerSet = quiz.getQuizOptions().stream()
                    .filter(QuizOption::getIsAnswer)
                    .map(option -> option.getId().getOptionNum())
                    .collect(Collectors.toSet());

            Set<Integer> allOptionNums = quiz.getQuizOptions().stream()
                    .map(option -> option.getId().getOptionNum())
                    .collect(Collectors.toSet());

            Set<Integer> selectedSet = new HashSet<>(answer.getSelectedOptions());

            if(!allOptionNums.containsAll(selectedSet)) {
                throw new RestException(ErrorCode.GLOBAL_BAD_REQUEST);
            }

            int isCorrect = selectedSet.equals(answerSet) ? 1 : 0;
            correctCount += isCorrect;

            QuizRecord record = QuizRecord.builder()
                    .id(QuizRecordId.of(user.getUser(), quiz))
                    .correctCount(isCorrect)
                    .build();
            toSave.add(record);
        }

        // 퀴즈 기록 저장
        quizRecordRepository.saveAll(toSave);

        LocalDateTime now = LocalDateTime.now();

        return QuizRecordDto.QuizSubmitResponse.from(user.getUser(), studyGroup, quizzes.size(), correctCount, now);
    }

    private void cannotWriteIfNotMember(StudyGroup studyGroup, UserDetails user) {
        if(!memberQueryRepository.isMember(studyGroup, user.getUser())) {
            throw new RestException(ErrorCode.STUDY_GROUP_NOT_MEMBER);
        }
    }

    private void cannotWriteWhenAlreadySubmitted(StudyGroup studyGroup, UserDetails user, Integer step) {
        if(quizRecordQueryRepository.existsUserSubmittedStep(studyGroup, user.getUser(), step)) {
            throw new RestException(ErrorCode.QUIZ_ALREADY_SUBMITTED_IN_STEP);
        }
    }

    @Transactional(readOnly = true)
    public PagedModel<QuizRecordDto.QuizRecordResponse> paginateQuizRecords(
            Long studyGroupId,
            QuizRecordDto.SearchConditions searchConditions,
            UserDetails user,
            Pageable pageable) {

        StudyGroup studyGroup = studyGroupRepository.findById(studyGroupId)
                .orElseThrow(() -> new RestException(ErrorCode.STUDY_GROUP_NOT_FOUND));

        cannotViewIfNotMember(studyGroup, user);

        Page<QuizRecordDto.QuizRecordResponse> result = quizRecordQueryRepository.paginateQuizRecords(
                studyGroup, user, searchConditions, pageable);

        return new PagedModel<>(result);
    }
}
