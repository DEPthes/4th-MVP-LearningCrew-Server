package com.depth.learningcrew.domain.studygroup.controller;

import com.depth.learningcrew.domain.studygroup.dto.DibsDto;
import com.depth.learningcrew.domain.studygroup.service.DibsService;
import com.depth.learningcrew.system.security.model.UserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-groups/{groupId}/dibs")
@Tag(name = "Study Group")
public class DibsController {

    private final DibsService dibsService;

    @PostMapping
    @Operation(summary = "스터디 그룹 찜 토글", description = "로그인한 사용자가 특정 스터디 그룹을 찜하거나 찜 해제합니다.")
    public DibsDto.DibsResponse toggleDibs(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetails user
    ) {
        return dibsService.toggleDibs(groupId, user);
    }
}
