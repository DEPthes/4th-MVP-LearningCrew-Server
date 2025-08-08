package com.depth.learningcrew.domain.note.service;

import com.depth.learningcrew.domain.file.entity.NoteAttachedFile;
import com.depth.learningcrew.domain.file.entity.NoteImageFile;
import com.depth.learningcrew.domain.file.handler.FileHandler;
import com.depth.learningcrew.domain.note.dto.NoteDto;
import com.depth.learningcrew.domain.note.entity.Note;
import com.depth.learningcrew.domain.note.repository.NoteRepository;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.entity.StudyStep;
import com.depth.learningcrew.domain.studygroup.entity.StudyStepId;
import com.depth.learningcrew.domain.studygroup.repository.MemberRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyStepRepository;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class NoteServiceTest {

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private FileHandler fileHandler;

    @InjectMocks
    private NoteService noteService;

    private Long groupId;
    private Integer step;
    private StudyGroup studyGroup;
    private User user;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        groupId = 1L;
        step = 2;

        user = User.builder()
                .id(10L)
                .email("test@example.com")
                .password("pw")
                .nickname("tester")
                .build();

        userDetails = UserDetails.builder()
                .user(user)
                .build();

        studyGroup = StudyGroup.builder()
                .id(groupId)
                .name("테스트 스터디")
                .summary("요약")
                .maxMembers(10)
                .currentStep(step)
                .owner(user)
                .build();
    }

    private NoteDto.NoteCreateRequest req(boolean withFile, boolean withImage) {
        NoteDto.NoteCreateRequest.NoteCreateRequestBuilder b = NoteDto.NoteCreateRequest.builder()
                .title("노트 제목")
                .content("노트 내용");

        if (withFile) {
            MockMultipartFile doc = new MockMultipartFile(
                    "attachedFiles",
                    "note-doc.txt",
                    "text/plain",
                    "dummy text content".getBytes()
            );
            b.attachedFiles(List.of(doc));
        }

        if (withImage) {
            MockMultipartFile image = new MockMultipartFile(
                    "attachedImages",
                    "note-image.jpg",
                    "image/jpeg",
                    "dummy image bytes".getBytes()
            );
            b.attachedImages(List.of(image));
        }

        return b.build();
    }

    @Test
    @DisplayName("스터디그룹의 멤버이고 현재 단계면 노트를 작성할 수 있다")
    void createNote_success_whenMemberAndCurrentStep() {
        // given
        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
        when(memberRepository.existsById_UserAndId_StudyGroup(user, studyGroup)).thenReturn(true);
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        var res = noteService.createNote(groupId, step, req(false, false), userDetails);

        // then
        assertThat(res).isNotNull();
        verify(studyGroupRepository).findById(groupId);
        verify(memberRepository).existsById_UserAndId_StudyGroup(user, studyGroup);
        verify(noteRepository).save(any(Note.class));
        verifyNoInteractions(fileHandler);

        var captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getStep()).isEqualTo(step);
    }

    @Test
    @DisplayName("스터디그룹의 멤버가 아니면 예외가 발생한다")
    void createNote_fail_whenNotMember() {
        // given
        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
        when(memberRepository.existsById_UserAndId_StudyGroup(user, studyGroup)).thenReturn(false);

        // when / then
        assertThatThrownBy(() ->
                noteService.createNote(groupId, step, req(false, false), userDetails)
        ).isInstanceOf(RestException.class);

        verifyNoInteractions(noteRepository, fileHandler);
    }

    @Test
    @DisplayName("현재 스텝이 아니면 예외가 발생한다")
    void createNote_fail_whenNotCurrentStep() throws Exception {
        // given
        StudyGroup notCurrent = StudyGroup.builder()
                .id(groupId)
                .name("테스트 스터디")
                .summary("요약")
                .maxMembers(10)
                .currentStep(step + 1)
                .owner(user)
                .build();

        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(notCurrent));
        when(memberRepository.existsById_UserAndId_StudyGroup(user, notCurrent)).thenReturn(true);

        // when / then
        assertThatThrownBy(() ->
                noteService.createNote(groupId, step, req(false, false), userDetails)
        ).isInstanceOf(RestException.class);

        verifyNoInteractions(noteRepository, fileHandler);
    }

    @Test
    @DisplayName("스터디그룹의 멤버이며 현재 스텝이고 파일/이미지가 포함되면 첨부가 저장된다")
    void createNote_success_withFilesAndImages() {
        // given
        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
        when(memberRepository.existsById_UserAndId_StudyGroup(user, studyGroup)).thenReturn(true);
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        var res = noteService.createNote(groupId, step, req(true, true), userDetails);

        // then
        assertThat(res).isNotNull();
        verify(fileHandler, times(1)).saveFile(any(MultipartFile.class), any(NoteAttachedFile.class));
        verify(fileHandler, times(1)).saveFile(any(MultipartFile.class), any(NoteImageFile.class));

        var captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());
        assertThat(captor.getValue().getAttachedFiles()).hasSize(1);
        assertThat(captor.getValue().getAttachedImages()).hasSize(1);
    }
}

