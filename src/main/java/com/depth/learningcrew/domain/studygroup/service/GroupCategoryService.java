package com.depth.learningcrew.domain.studygroup.service;

import com.depth.learningcrew.domain.studygroup.dto.GroupCategoryDto;
import com.depth.learningcrew.domain.studygroup.entity.GroupCategory;
import com.depth.learningcrew.domain.studygroup.repository.GroupCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupCategoryService {

    private final GroupCategoryRepository groupCategoryRepository;

    @Transactional(readOnly = true)
    public List<GroupCategoryDto.GroupCategoryResponse> getGroupCategories() {
        List<GroupCategory> groupCategories = groupCategoryRepository.findAll();
        return groupCategories.stream()
                .map(GroupCategoryDto.GroupCategoryResponse::from)
                .collect(Collectors.toList());
    }
}
