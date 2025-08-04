package com.depth.learningcrew.domain.studygroup.service;

import com.depth.learningcrew.domain.studygroup.dto.DibsDto;
import com.depth.learningcrew.domain.studygroup.entity.Dibs;
import com.depth.learningcrew.domain.studygroup.entity.DibsId;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.DibsRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DibsService {

    private final DibsRepository dibsRepository;
    private final StudyGroupRepository studyGroupRepository;

    @Transactional
    public DibsDto.DibsResponse toggleDibs(Long groupId, UserDetails user) {
        StudyGroup found = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new RestException(ErrorCode.STUDY_GROUP_NOT_FOUND));

        DibsId dibsId = new DibsId(user.getUser(), found);

        if (dibsRepository.existsById(dibsId)) {
            dibsRepository.deleteById(dibsId);
            return DibsDto.DibsResponse.from(false);
        } else {
            dibsRepository.save(Dibs.from(dibsId));
            return DibsDto.DibsResponse.from(true);
        }
    }
}
