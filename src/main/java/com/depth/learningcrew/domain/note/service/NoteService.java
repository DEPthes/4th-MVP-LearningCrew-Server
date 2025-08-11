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

        if (noteRepository.existsByStudyGroup_IdAndStepAndCreatedBy_Id(groupId, step, user.getUser().getId())) {
            throw new RestException(ErrorCode.NOTE_ALREADY_EXISTS_IN_STEP);
        }

        Note note = request.toEntity();
        note.setStep(step);
        note.setStudyGroup(studyGroup);

        Note saved = noteRepository.save(note);
        saveAttachedFiles(request.getAttachedFiles(), saved);
        saveAttachedImages(request.getAttachedImages(), saved);

        return NoteDto.NoteResponse.from(saved);
    }

    @Transactional
    public NoteDto.NoteResponse updateNote(
            Long noteId,
            NoteDto.NoteUpdateRequest request,
            UserDetails user) {

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RestException(ErrorCode.NOTE_NOT_FOUND));

        note.canUpdateBy(user.getUser());

        if (request.getTitle() != null) {
            note.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            note.setContent(request.getContent());
        }

        deleteAttachedFiles(request.getDeletedAttachedFiles(), note);
        deleteAttachedImages(request.getDeletedAttachedImages(), note);
        saveAttachedFiles(request.getNewAttachedFiles(), note);
        saveAttachedImages(request.getNewAttachedImages(), note);

        return NoteDto.NoteResponse.from(note);
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

    private void saveAttachedFiles(List<MultipartFile> files, Note note) {
        if (files != null && !files.isEmpty()) {
            files.forEach(file -> {
                if (file == null || file.isEmpty()) return;
                NoteAttachedFile attachedFile = NoteAttachedFile.from(file);
                if (attachedFile == null) return;
                note.addAttachedFile(attachedFile);
                fileHandler.saveFile(file, attachedFile);
            });
        }
    }

    private void saveAttachedImages(List<MultipartFile> images, Note note) {
        if (images != null && !images.isEmpty()) {
            images.forEach(image -> {
                if (image == null || image.isEmpty()) return;
                NoteImageFile imageFile = NoteImageFile.from(image);
                if (imageFile == null) return;
                note.addAttachedImage(imageFile);
                fileHandler.saveFile(image, imageFile);
            });
        }
    }

    public void deleteAttachedFiles(List<String> fileIds, Note note) {
        if (fileIds != null && !fileIds.isEmpty()) {
            fileIds.forEach(fileId -> {
                NoteAttachedFile attachedFile = note.getAttachedFiles().stream()
                        .filter(f -> f.getUuid().equals(fileId))
                        .findFirst()
                        .orElse(null);
                if (attachedFile != null) {
                    note.removeAttachedFile(attachedFile);
                    fileHandler.deleteFile(attachedFile);
                }
            });
        }
    }

    public void deleteAttachedImages(List<String> imageIds, Note note) {
        if (imageIds != null && !imageIds.isEmpty()) {
            imageIds.forEach(imageId -> {
                NoteImageFile imageFile = note.getAttachedImages().stream()
                        .filter(f -> f.getUuid().equals(imageId))
                        .findFirst()
                        .orElse(null);
                if (imageFile != null) {
                    note.removeAttachedImage(imageFile);
                    fileHandler.deleteFile(imageFile);
                }
            });
        }
    }

    @Transactional(readOnly = true)
    public NoteDto.NoteResponse getNoteDetail(Long noteId, UserDetails user) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RestException(ErrorCode.NOTE_NOT_FOUND));

        StudyGroup studyGroup = note.getStudyGroup();

        if (!memberQueryRepository.isMember(studyGroup, user.getUser())) {
            throw new RestException(ErrorCode.STUDY_GROUP_NOT_MEMBER);
        }

        return NoteDto.NoteResponse.from(note);
    }
}