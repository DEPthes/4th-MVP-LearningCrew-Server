package com.depth.learningcrew.domain.studygroup.controller;

import com.depth.learningcrew.domain.studygroup.dto.DibsDto;
import com.depth.learningcrew.domain.studygroup.service.DibsService;
import com.depth.learningcrew.system.security.model.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-groups/{groupId}/dibs")
public class DibsController {

    private final DibsService dibsService;

    @PostMapping
    public DibsDto.DibsResponse toggleDibs(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetails user
    ) {
        return dibsService.toggleDibs(groupId, user);
    }
}
