package com.depth.learningcrew.domain.studygroup.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.depth.learningcrew.domain.studygroup.service.StudyGroupService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyGroupScheduler {
  private final StudyGroupService studyGroupService;

  @Scheduled(cron = "0 0 0 * * *")
  @Transactional
  public void updateStudyGroupCurrentStep() {
    log.info("스터디 그룹의 현재 단계 업데이트 시작");
    studyGroupService.updateStudyGroupCurrentStep();
    log.info("스터디 그룹의 현재 단계 업데이트 종료");
  }
}
