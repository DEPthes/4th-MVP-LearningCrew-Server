package com.depth.learningcrew.domain.studygroup.repository;

import static com.depth.learningcrew.domain.studygroup.entity.QApplication.application;
import static com.depth.learningcrew.domain.studygroup.entity.QStudyGroup.studyGroup;
import static com.depth.learningcrew.domain.user.entity.QUser.user;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.depth.learningcrew.domain.studygroup.dto.ApplicationDto;
import com.depth.learningcrew.domain.studygroup.entity.Application;
import com.depth.learningcrew.system.security.model.UserDetails;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ApplicationQueryRepository {
  private final JPAQueryFactory queryFactory;

  /**
   * 스터디 그룹의 가입 신청 목록을 페이지네이션하여 조회합니다.
   *
   * @param groupId          스터디 그룹 ID
   * @param searchConditions 검색 조건
   * @param userDetails      로그인 사용자 정보 (owner 권한 확인용)
   * @param pageable         페이지 정보
   * @return 페이지네이션된 가입 신청 목록
   */
  public Page<ApplicationDto.ApplicationResponse> paginateApplicationsByGroupId(
      Integer groupId,
      ApplicationDto.SearchConditions searchConditions,
      UserDetails userDetails,
      Pageable pageable) {

    var query = queryFactory
        .select(application)
        .from(application)
        .join(application.id.studyGroup, studyGroup)
        .join(application.id.user, user)
        .where(studyGroup.id.eq(groupId),
            studyGroup.owner.id.eq(userDetails.getUser().getId()));

    // 검색 조건 적용
    var searchCondition = buildSearchCondition(searchConditions);
    if (searchCondition != null) {
      query = query.where(searchCondition);
    }

    // 정렬 조건 적용
    applySorting(query, searchConditions);

    List<Application> results = query
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    // 총 개수 조회
    var countQuery = queryFactory
        .select(application.count())
        .from(application)
        .join(application.id.studyGroup, studyGroup)
        .where(studyGroup.id.eq(groupId),
            studyGroup.owner.id.eq(userDetails.getUser().getId()));

    if (searchCondition != null) {
      countQuery = countQuery.where(searchCondition);
    }

    Long totalCount = countQuery.fetchOne();

    List<ApplicationDto.ApplicationResponse> content = results.stream()
        .map(ApplicationDto.ApplicationResponse::from)
        .toList();

    return new PageImpl<>(content, pageable, totalCount != null ? totalCount : 0L);
  }

  private BooleanExpression buildSearchCondition(ApplicationDto.SearchConditions searchConditions) {
    BooleanExpression predicate = null;

    // 상태 필터링
    if (searchConditions.getState() != null) {
      predicate = application.state.eq(searchConditions.getState());
    }

    // 이름 검색
    if (StringUtils.hasText(searchConditions.getName())) {
      BooleanExpression namePredicate = user.nickname.containsIgnoreCase(searchConditions.getName());
      if (predicate != null) {
        predicate = predicate.and(namePredicate);
      } else {
        predicate = namePredicate;
      }
    }

    return predicate;
  }

  private void applySorting(JPAQuery<Application> query, ApplicationDto.SearchConditions searchConditions) {
    String sort = searchConditions.getSort();
    String order = searchConditions.getOrder();

    if ("alphabet".equals(sort)) {
      if ("asc".equals(order)) {
        query.orderBy(user.nickname.asc());
      } else {
        query.orderBy(user.nickname.desc());
      }
    } else { // created_at (기본값)
      if ("asc".equals(order)) {
        query.orderBy(application.createdAt.asc());
      } else {
        query.orderBy(application.createdAt.desc());
      }
    }
  }
}