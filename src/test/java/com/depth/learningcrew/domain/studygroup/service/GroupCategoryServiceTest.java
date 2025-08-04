package com.depth.learningcrew.domain.studygroup.service;

import com.depth.learningcrew.domain.studygroup.entity.GroupCategory;
import com.depth.learningcrew.domain.studygroup.repository.GroupCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GroupCategoryServiceTest {

    private GroupCategoryRepository groupCategoryRepository;
    private GroupCategoryService groupCategoryService;

    @BeforeEach
    void setUp() {
        groupCategoryRepository = mock(GroupCategoryRepository.class);
        groupCategoryService = new GroupCategoryService(groupCategoryRepository);
    }

    @Test
    @DisplayName("기존 카테고리는 재사용하고, 없는 카테고리는 새로 생성하여 반환한다")
    void findOrCreateByNames_shouldReuseExistingAndCreateNew() {
        // given
        String existingName = "기존카테고리";
        String newName = "새카테고리";

        GroupCategory existingCategory = GroupCategory.builder().id(1).name(existingName).build();
        GroupCategory newCategory = GroupCategory.builder().id(2).name(newName).build();

        when(groupCategoryRepository.findByName(existingName)).thenReturn(Optional.of(existingCategory));
        when(groupCategoryRepository.findByName(newName)).thenReturn(Optional.empty());
        when(groupCategoryRepository.save(any(GroupCategory.class))).thenReturn(newCategory);

        // when
        List<GroupCategory> result = groupCategoryService.findOrCreateByNames(List.of(existingName, newName));

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactlyInAnyOrder(existingName, newName);

        verify(groupCategoryRepository, times(1)).findByName(existingName);
        verify(groupCategoryRepository, times(1)).findByName(newName);
        verify(groupCategoryRepository, times(1)).save(any(GroupCategory.class));
    }
}
