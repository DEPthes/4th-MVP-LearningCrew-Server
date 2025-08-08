package com.depth.learningcrew.domain.note.service;

import com.depth.learningcrew.domain.file.entity.NoteAttachedFile;
import com.depth.learningcrew.domain.file.entity.NoteImageFile;
import com.depth.learningcrew.domain.file.handler.FileHandler;
import com.depth.learningcrew.domain.note.dto.NoteDto;
import com.depth.learningcrew.domain.note.entity.Note;
import com.depth.learningcrew.domain.note.repository.NoteRepository;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.MemberRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final StudyGroupRepository studyGroupRepository;
    private final MemberRepository memberRepository;
    private final NoteRepository noteRepository;
    private final FileHandler fileHandler;

    @Transactional
    public NoteDto.NoteResponse createNote(Long groupId,
                                           Integer step,
                                           NoteDto.NoteCreateRequest request,
                                           UserDetails userDetails) {
        User user = userDetails.getUser();

        StudyGroup studyGroup = getStudyGroup(groupId);
        validateStudyGroupMember(user, studyGroup);
        validateWritableStep(studyGroup, step);

        Note note = Note.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .step(step)
                .studyGroup(studyGroup)
                .createdBy(user)
                .lastModifiedBy(user)
                .build();

        saveNewAttachedFiles(request.getAttachedFiles(), note);
        saveNewAttachedImages(request.getAttachedImages(), note);

        Note saved = noteRepository.save(note);
        return NoteDto.NoteResponse.from(saved);
    }

    private StudyGroup getStudyGroup(Long groupId) {
        return studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new RestException(ErrorCode.STUDY_GROUP_NOT_FOUND));
    }

    private void validateStudyGroupMember(User user, StudyGroup group) {
        if (!memberRepository.existsById_UserAndId_StudyGroup(user, group)) {
            throw new RestException(ErrorCode.STUDY_GROUP_NOT_MEMBER);
        }
    }

    private void validateWritableStep(StudyGroup studyGroup, Integer step) {
        Integer currentStep = studyGroup.getCurrentStep();
        if (currentStep == null || !currentStep.equals(step)) {
            throw new RestException(ErrorCode.STUDY_GROUP_STEP_NOT_WRITABLE);
        }
    }

    private void saveNewAttachedFiles(List<MultipartFile> files, Note note) {
        if (files != null && !files.isEmpty()) {
            files.forEach(file -> {
                NoteAttachedFile attachedFile = NoteAttachedFile.from(file);
                note.addAttachedFile(attachedFile);
                fileHandler.saveFile(file, attachedFile);
            });
        }
    }

    private void saveNewAttachedImages(List<MultipartFile> images, Note note) {
        if (images != null && !images.isEmpty()) {
            images.forEach(image -> {
                NoteImageFile imageFile = NoteImageFile.from(image);
                note.addAttachedImage(imageFile);
                fileHandler.saveFile(image, imageFile);
            });
        }
    }
}