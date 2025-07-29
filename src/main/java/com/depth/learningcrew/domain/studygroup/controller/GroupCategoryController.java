package com.depth.learningcrew.domain.studygroup.controller;

import com.depth.learningcrew.domain.studygroup.dto.GroupCategoryDto;
import com.depth.learningcrew.domain.studygroup.service.GroupCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study-groups/categories")
public class GroupCategoryController {

    private final GroupCategoryService groupCategoryService;

    @GetMapping
    public List<GroupCategoryDto.GroupCategoryResponse> getGroupCategories() {
        return groupCategoryService.getGroupCategories();
    }
}
