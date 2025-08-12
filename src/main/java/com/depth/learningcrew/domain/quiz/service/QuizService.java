package com.depth.learningcrew.domain.quiz.service;

import com.depth.learningcrew.domain.quiz.dto.QuizDto;
import com.depth.learningcrew.domain.quiz.repository.QuizQueryRepository;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.MemberQueryRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final StudyGroupRepository studyGroupRepository;
    private final MemberQueryRepository memberQueryRepository;
    private final QuizQueryRepository quizQueryRepository;

    @Transactional(readOnly = true)
    public List<QuizDto.QuizResponse> getStepQuizzes(
            Long studyGroupId,
            Integer step,
            UserDetails user) {

        StudyGroup studyGroup = studyGroupRepository.findById(studyGroupId)
                .orElseThrow(() -> new RestException(ErrorCode.STUDY_GROUP_NOT_FOUND));

        cannotReadWhenNotMember(studyGroup, user);

        return quizQueryRepository.findAllOfStepWithOptions(studyGroup, step)
                .stream()
                .map(QuizDto.QuizResponse::from)
                .toList();
    }

    private void cannotReadWhenNotMember(StudyGroup studyGroup, UserDetails user) {
        if(!memberQueryRepository.isMember(studyGroup, user.getUser())) {
            throw new RestException(ErrorCode.STUDY_GROUP_NOT_MEMBER);
        }
    }
}
