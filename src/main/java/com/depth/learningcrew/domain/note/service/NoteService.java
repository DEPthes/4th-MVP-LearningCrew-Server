package com.depth.learningcrew.domain.note.service;

import com.depth.learningcrew.domain.file.entity.NoteAttachedFile;
import com.depth.learningcrew.domain.file.entity.NoteImageFile;
import com.depth.learningcrew.domain.file.handler.FileHandler;
import com.depth.learningcrew.domain.note.dto.NoteDto;
import com.depth.learningcrew.domain.note.entity.Note;
import com.depth.learningcrew.domain.note.repository.NoteRepository;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.repository.MemberQueryRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final StudyGroupRepository studyGroupRepository;
    private final MemberQueryRepository memberQueryRepository;
    private final NoteRepository noteRepository;
    private final FileHandler fileHandler;

    @Transactional
    public NoteDto.NoteResponse createNote(
            Long groupId,
            Integer step,
            NoteDto.NoteCreateRequest request,
            UserDetails user) {

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new RestException(ErrorCode.STUDY_GROUP_NOT_FOUND));

        cannotCreateWhenNotCurrentStep(studyGroup, step);
        cannotCreateWhenNotMember(studyGroup, user);

        Note note = request.toEntity();
        note.setStep(step);
        note.setStudyGroup(studyGroup);

        Note saved = noteRepository.save(note);
        saveNewAttachedFiles(request.getAttachedFiles(), saved);
        saveNewAttachedImages(request.getAttachedImages(), saved);

        return NoteDto.NoteResponse.from(saved);
    }

    private void cannotCreateWhenNotMember(StudyGroup studyGroup, UserDetails user) {
        if (!memberQueryRepository.isMember(studyGroup, user.getUser())) {
            throw new RestException(ErrorCode.STUDY_GROUP_NOT_MEMBER);
        }
    }

    private void cannotCreateWhenNotCurrentStep(StudyGroup studyGroup, Integer step) {
        if (!Objects.equals(studyGroup.getCurrentStep(), step)) {
            throw new RestException(ErrorCode.STUDY_GROUP_NOT_CURRENT_STEP);
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