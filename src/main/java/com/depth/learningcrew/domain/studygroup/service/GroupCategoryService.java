package com.depth.learningcrew.domain.studygroup.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.depth.learningcrew.domain.studygroup.dto.GroupCategoryDto;
import com.depth.learningcrew.domain.studygroup.entity.GroupCategory;
import com.depth.learningcrew.domain.studygroup.repository.GroupCategoryRepository;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;

import lombok.RequiredArgsConstructor;

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

    @Transactional(readOnly = true)
    public List<GroupCategory> findByNames(List<String> names) {
        List<GroupCategory> categories = groupCategoryRepository.findAll();
        List<GroupCategory> result = categories.stream()
                .filter(cat -> names.contains(cat.getName()))
                .collect(Collectors.toList());
        if (result.size() != names.size()) {
            throw new RestException(ErrorCode.GLOBAL_BAD_REQUEST, "존재하지 않는 카테고리 이름이 포함되어 있습니다.");
        }
        return result;
    }
}
