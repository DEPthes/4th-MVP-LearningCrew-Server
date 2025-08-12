package com.depth.learningcrew.system.initializer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.depth.learningcrew.domain.studygroup.entity.GroupCategory;
import com.depth.learningcrew.domain.studygroup.repository.GroupCategoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class GroupCategoryPresetInitializer implements CommandLineRunner {

  private final GroupCategoryRepository groupCategoryRepository;

  @Override
  @Transactional
  public void run(String... args) {
    if (groupCategoryRepository.count() > 0) {
      log.info("그룹 카테고리 프리셋 로드 생략: 기존 데이터가 존재합니다.");
      return;
    }

    List<String> presetNames = Arrays.asList(
        "언어",
        "디자인, 아트",
        "경영, 마케팅",
        "개발, 프로그래밍",
        "게임 개발",
        "보안, 네트워크",
        "커리어",
        "하드웨어",
        "대입 수능");

    List<GroupCategory> categories = presetNames.stream()
        .map(name -> GroupCategory.builder().name(name).build())
        .collect(Collectors.toList());

    groupCategoryRepository.saveAll(categories);
    log.info("그룹 카테고리 프리셋 {}건을 초기 로드했습니다.", categories.size());
  }
}
