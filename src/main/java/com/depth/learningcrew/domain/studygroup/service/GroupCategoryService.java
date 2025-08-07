package com.depth.learningcrew.domain.studygroup.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.system.security.model.UserDetails;
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

    @Transactional
    public List<GroupCategory> findOrCreateByNames(List<String> names) {
        List<GroupCategory> result = new ArrayList<>();

        for (String name : names) {
            GroupCategory category = groupCategoryRepository.findByName(name)
                    .orElseGet(() -> groupCategoryRepository.save(
                            GroupCategory.builder().name(name).build()));
            result.add(category);
        }
        return result;
    }

    @Transactional
    public GroupCategoryDto.GroupCategoryUpdateResponse updateGroupCategory(
            Integer categoryId,
            GroupCategoryDto.GroupCategoryUpdateRequest request,
            UserDetails userDetails) {
        GroupCategory groupCategory = groupCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RestException(ErrorCode.GLOBAL_NOT_FOUND));

        groupCategory.canUpdateBy(userDetails);

        request.applyTo(groupCategory);

        return GroupCategoryDto.GroupCategoryUpdateResponse.from(groupCategory);
    }

    @Transactional
    public GroupCategoryDto.GroupCategoryResponse createGroupCategory(
            GroupCategoryDto.GroupCategoryCreateRequest request,
            UserDetails userDetails) {
        cannotCreateWhenAlreadyExists(request.getName());

        if(!userDetails.getUser().getRole().equals(Role.ADMIN)) {
            throw new RestException(ErrorCode.AUTH_FORBIDDEN);
        }

        GroupCategory groupCategory = GroupCategory.builder()
                .name(request.getName())
                .build();

        GroupCategory savedCategory = groupCategoryRepository.save(groupCategory);

        return GroupCategoryDto.GroupCategoryResponse.from(savedCategory);
    }

    private void cannotCreateWhenAlreadyExists(String name) {
        if(groupCategoryRepository.existsByName(name)) {
            throw new RestException(ErrorCode.GROUP_CATEGORY_ALREADY_EXIST);
        }
    }
}
