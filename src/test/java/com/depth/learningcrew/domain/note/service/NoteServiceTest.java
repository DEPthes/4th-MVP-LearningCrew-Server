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
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.security.model.UserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class NoteServiceTest {

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private MemberQueryRepository memberQueryRepository;

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
    private Note savedNote;

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

        savedNote = Note.builder()
                .id(1L)
                .title("테스트 노트 제목")
                .content("테스트 노트 내용")
                .step(step)
                .studyGroup(studyGroup)
                .createdBy(user)
                .lastModifiedBy(user)
                .attachedFiles(new ArrayList<>())
                .attachedImages(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("첨부 파일과 이미지가 포함된 노트를 생성할 수 있다")
    void createNote_WithAttachedFiles_ShouldCreateSuccessfully() {
        // given
        MockMultipartFile file1 = new MockMultipartFile("attachedFiles", "test1.pdf", "application/pdf", "test".getBytes());
        MockMultipartFile image1 = new MockMultipartFile("attachedImages", "test1.jpg", "image/jpeg", "img".getBytes());

        NoteDto.NoteCreateRequest requestWithFiles = NoteDto.NoteCreateRequest.builder()
                .title(savedNote.getTitle())
                .content(savedNote.getContent())
                .attachedFiles(List.of(file1))
                .attachedImages(List.of(image1))
                .build();

        when(studyGroupRepository.findById(groupId)).thenReturn(Optional.of(studyGroup));
        when(memberQueryRepository.isMember(studyGroup, user)).thenReturn(true);
        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        // when
        var result = noteService.createNote(groupId, step, requestWithFiles, userDetails);

        // then
        verify(noteRepository, times(1)).save(any(Note.class));
        verify(fileHandler, times(1)).saveFile(eq(file1), any(NoteAttachedFile.class));
        verify(fileHandler, times(1)).saveFile(eq(image1), any(NoteImageFile.class));
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedNote.getId());
        assertThat(result.getTitle()).isEqualTo(savedNote.getTitle());
        assertThat(result.getStep()).isEqualTo(savedNote.getStep());
    }
}

