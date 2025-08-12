package com.depth.learningcrew.domain.note.controller;

import com.depth.learningcrew.domain.note.dto.NoteDto;
import com.depth.learningcrew.domain.note.service.NoteService;
import com.depth.learningcrew.system.security.model.UserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "NOTE", description = "노트 API")
public class NoteController {
    private final NoteService noteService;

    @PostMapping(value = "/study-groups/{groupId}/steps/{step}/notes")
    @Operation(summary = "노트 생성", description = "새로운 노트를 생성합니다.")
    @ApiResponse(responseCode = "201", description = "노트 생성 성공")
    @ResponseStatus(HttpStatus.CREATED)
    public NoteDto.NoteResponse createNote(
            @Parameter(description = "스터디 그룹 ID", example = "1") @PathVariable Long groupId,
            @Parameter(description = "스터디 스텝 번호", example = "1") @PathVariable Integer step,
            @Parameter(description = "노트 작성 요청") @Valid @ModelAttribute NoteDto.NoteCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return noteService.createNote(groupId, step, request, userDetails);
    }

    @PatchMapping(value = "/notes/{noteId}")
    @Operation(summary = "노트 수정", description = "노트를 수정합니다. 노트 작성자 또는 스터디 그룹 주최자만 수정할 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "노트 수정 성공")
    public NoteDto.NoteResponse updateNote(
            @Parameter(description = "노트 ID", example = "1") @PathVariable Long noteId,
            @Parameter(description = "노트 수정 요청") @Valid @ModelAttribute NoteDto.NoteUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return noteService.updateNote(noteId, request, userDetails);
    }

    @GetMapping(value = "/notes/{noteId}")
    @Operation(summary = "특정 노트 상세 조회", description = "노트 상세 정보를 조회합니다. 스터디 그룹의 멤버만 조회할 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "노트 상세 조회 성공")
    public NoteDto.NoteResponse getNoteDetail(
            @Parameter(description = "노트 ID", example = "1") @PathVariable Long noteId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return noteService.getNoteDetail(noteId, userDetails);
    }

    @GetMapping(value = "/study-groups/{groupId}/steps/{step}/notes")
    @Operation(summary = "스터디 그룹 공유 노트 목록 조회", description = "스터디 그룹의 멤버가 다른 멤버들의 공유 노트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "공유 노트 목록 조회 성공")
    public List<NoteDto.SharedNoteResponse> getSharedNotes(
            @Parameter(description = "스터디 그룹 ID", example = "1") @PathVariable Long groupId,
            @Parameter(description = "스터디 스텝 번호", example = "1") @PathVariable Integer step,
            @ModelAttribute @ParameterObject NoteDto.SearchConditions searchConditions,
            @AuthenticationPrincipal UserDetails userDetails) {

        return noteService.getSharedNotes(groupId, step, searchConditions, userDetails);
    }
}
