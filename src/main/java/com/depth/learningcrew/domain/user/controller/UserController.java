package com.depth.learningcrew.domain.user.controller;

import com.depth.learningcrew.domain.user.dto.UserDto;
import com.depth.learningcrew.domain.user.service.UserService;
import com.depth.learningcrew.system.security.model.UserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User", description = "사용자 API")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인된 사용자의 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "내 정보 조회 성공")
    public UserDto.UserResponse whoami(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return UserDto.UserResponse.from(userDetails.getUser());
    }

    @PatchMapping("/me")
    @Operation(summary = "내 정보 수정", description = "현재 로그인된 사용자의 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "내 정보 수정 성공")
    public UserDto.UserUpdateResponse update(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute UserDto.UserUpdateRequest request
    ) {
        return userService.update(userDetails.getUser(), request);
    }
}