package com.depth.learningcrew.system.initializer;

import com.depth.learningcrew.domain.studygroup.entity.GroupCategory;
import com.depth.learningcrew.domain.studygroup.repository.GroupCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryInitializer implements CommandLineRunner {

    private final GroupCategoryRepository groupCategoryRepository;

    @Override
    public void run(String... args) {
        if (groupCategoryRepository.count() == 0) {
            log.info("Initializing categories");
            List<GroupCategory> categories = List.of(
                    GroupCategory.builder().name("언어").build(),
                    GroupCategory.builder().name("디자인, 아트").build(),
                    GroupCategory.builder().name("경영, 마케팅").build(),
                    GroupCategory.builder().name("개발, 프로그래밍").build(),
                    GroupCategory.builder().name("게임 개발").build(),
                    GroupCategory.builder().name("보안, 네트워크").build(),
                    GroupCategory.builder().name("커리어").build(),
                    GroupCategory.builder().name("하드웨어").build(),
                    GroupCategory.builder().name("대입 수능").build()
            );
            groupCategoryRepository.saveAll(categories);
            log.info("Categories initialized");
        }
    }
}
