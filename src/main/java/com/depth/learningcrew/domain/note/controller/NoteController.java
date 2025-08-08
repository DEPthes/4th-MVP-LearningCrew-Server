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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/study-groups")
@RequiredArgsConstructor
@Tag(name="NOTE", description = "노트 API")
public class NoteController {
    private final NoteService noteService;

    @PostMapping(value = "/{groupId}/steps/{step}/notes")
    @Operation(summary= "노트 생성", description = "새로운 노트를 생성합니다.")
    @ApiResponse(responseCode= "201", description = "노트 생성 성공")
    @ResponseStatus(HttpStatus.CREATED)
    public NoteDto.NoteResponse createNote(
            @Parameter(description = "스터디 그룹 ID", example = "1") @PathVariable Long groupId,
            @Parameter(description = "스터디 스텝 번호", example = "1") @PathVariable Integer step,
            @Parameter(description = "노트 작성 요청") @Valid @ModelAttribute NoteDto.NoteCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return noteService.createNote(groupId, step, request, userDetails);
    }
}
