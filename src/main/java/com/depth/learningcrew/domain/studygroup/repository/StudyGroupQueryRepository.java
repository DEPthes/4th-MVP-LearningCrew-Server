package com.depth.learningcrew.domain.studygroup.repository;

import static com.depth.learningcrew.domain.file.entity.QStudyGroupImage.studyGroupImage;
import static com.depth.learningcrew.domain.studygroup.entity.QDibs.dibs;
import static com.depth.learningcrew.domain.studygroup.entity.QGroupCategory.groupCategory;
import static com.depth.learningcrew.domain.studygroup.entity.QMember.member;
import static com.depth.learningcrew.domain.studygroup.entity.QStudyGroup.studyGroup;
import static com.depth.learningcrew.domain.studygroup.entity.QStudyStep.studyStep;
import static com.depth.learningcrew.domain.user.entity.QUser.user;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.querydsl.core.types.dsl.Expressions;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.depth.learningcrew.domain.studygroup.dto.StudyGroupDto;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.system.security.model.UserDetails;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StudyGroupQueryRepository {
    private final JPAQueryFactory queryFactory;

    public Page<StudyGroupDto.StudyGroupResponse> paginateByType(
            StudyGroupDto.SearchConditions searchConditions,
            @Nullable UserDetails user,
            Pageable pageable,
            StudyGroupFilterType filterType
    ) {
        var contentQuery = buildBaseQuery(searchConditions, user, filterType);
        applySorting(contentQuery, searchConditions);

        List<Tuple> results = contentQuery
                .leftJoin(studyGroup.studyGroupImage).fetchJoin()
                .leftJoin(studyGroup.owner).fetchJoin()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .distinct()
                .fetch();

        Long totalCount = buildCountQuery(searchConditions, user, filterType)
                .distinct()
                .fetchOne();

        List<StudyGroupDto.StudyGroupResponse> content = results.stream()
                .map(this::mapToDto)
                .toList();

        return new PageImpl<>(content, pageable, totalCount != null ? totalCount : 0L);
    }

    private JPAQuery<Tuple> buildBaseQuery(
            StudyGroupDto.SearchConditions searchConditions,
            @Nullable UserDetails user,
            StudyGroupFilterType filterType
    ) {
        JPAQuery<Tuple> query = queryFactory
                .select(
                        studyGroup,
                        Objects.nonNull(user) ?
                        JPAExpressions.selectOne()
                                .from(dibs)
                                .where(
                                        dibs.id.studyGroup.eq(studyGroup),
                                        dibs.id.user.id.eq(user.getUser().getId())
                                )
                                .exists() : Expressions.asBoolean(false)
                )
                .from(studyGroup);

        applyFilterTypeCondition(query, user, filterType);
        applySearchCondition(query, searchConditions);
        applyCategoryCondition(query, searchConditions);

        return query;
    }

    private JPAQuery<Long> buildCountQuery(
            StudyGroupDto.SearchConditions searchConditions,
            @Nullable UserDetails user,
            StudyGroupFilterType filterType
    ) {
        JPAQuery<Long> query = queryFactory
                .select(studyGroup.count())
                .from(studyGroup);

        applyFilterTypeCondition(query, user, filterType);
        applySearchCondition(query, searchConditions);
        applyCategoryCondition(query, searchConditions);

        return query;
    }

    private void applyFilterTypeCondition(JPAQuery<?> query, @Nullable UserDetails user, StudyGroupFilterType filterType) {
        if (user == null || user.getUser() == null){
            return;
        }

        if (filterType == StudyGroupFilterType.OWNED) {
            query.where(studyGroup.owner.id.eq(user.getUser().getId()));
        }

        if (filterType == StudyGroupFilterType.MEMBERED) {
            query
                    .join(member).on(member.id.studyGroup.eq(studyGroup))
                    .where(member.id.user.id.eq(user.getUser().getId()),
                            studyGroup.owner.id.ne(user.getUser().getId()));
        }
    }

    private void applySearchCondition(
            JPAQuery<?> query,
            StudyGroupDto.SearchConditions searchConditions) {

        Predicate searchCondition = buildSearchCondition(searchConditions);
        if (searchCondition != null) {
            query.where(searchCondition);
        }
    }

    private void applyCategoryCondition(
            JPAQuery<?> query,
            StudyGroupDto.SearchConditions searchConditions) {

        if (searchConditions.getCategoryId() != null) {
            query.join(studyGroup.categories, groupCategory)
                    .where(groupCategory.id.eq(searchConditions.getCategoryId()));
        }
    }

    private Predicate buildSearchCondition(
            StudyGroupDto.SearchConditions searchConditions) {
        if (!StringUtils.hasText(searchConditions.getSearchKeyword())) {
            return null;
        }

        String keyword = searchConditions.getSearchKeyword();
        return studyGroup.name.containsIgnoreCase(keyword)
                .or(studyGroup.categories.any().name.containsIgnoreCase(keyword));
    }

    private void applySorting(JPAQuery<Tuple> query,
                              StudyGroupDto.SearchConditions searchConditions) {
        String sort = searchConditions.getSort();
        String order = searchConditions.getOrder();

        if ("relative".equals(sort)) {
            if (StringUtils.hasText(searchConditions.getSearchKeyword())) {
                // relative 정렬 시 점수 계산 후 정렬
                NumberExpression<Integer> score = calculateRelevanceScore(
                        searchConditions.getSearchKeyword());
                if ("asc".equals(order)) {
                    query.orderBy(score.asc());
                } else {
                    query.orderBy(score.desc());
                }
            } else {
                // 검색어가 없는 경우 시작일 기준 정렬
                if ("asc".equals(order)) {
                    query.orderBy(studyGroup.startDate.asc());
                } else {
                    query.orderBy(studyGroup.startDate.desc());
                }
            }
        } else if ("alphabet".equals(sort)) {
            if ("asc".equals(order)) {
                query.orderBy(studyGroup.name.asc());
            } else {
                query.orderBy(studyGroup.name.desc());
            }
        } else { // created_at (기본값)
            if ("asc".equals(order)) {
                query.orderBy(studyGroup.createdAt.asc());
            } else {
                query.orderBy(studyGroup.createdAt.desc());
            }
        }
    }

    private NumberExpression<Integer> calculateRelevanceScore(String keyword) {
        return new CaseBuilder()
                .when(studyGroup.name.containsIgnoreCase(keyword)).then(3)
                .otherwise(0)
                .add(new CaseBuilder()
                        .when(studyGroup.categories.any().name.containsIgnoreCase(keyword)).then(2)
                        .otherwise(0));
    }

    private StudyGroupDto.StudyGroupResponse mapToDto(Tuple tuple) {
        StudyGroup entity = tuple.get(studyGroup);
        Boolean dibsValue = tuple.get(1, Boolean.class);
        return StudyGroupDto.StudyGroupResponse.from(
                Objects.requireNonNull(entity),
                dibsValue != null && dibsValue
        );
    }

    /**
     * 로그인한 사용자가 주최한 스터디 그룹 목록을 페이지네이션하여 조회합니다.
     *
     * @param searchConditions 검색 조건
     * @param user             로그인 사용자 정보
     * @param pageable         페이지 정보
     * @return 페이지네이션된 스터디 그룹 목록
     */
    public Page<StudyGroupDto.StudyGroupResponse> paginateMyOwnedGroups(
            StudyGroupDto.SearchConditions searchConditions,
            UserDetails user,
            Pageable pageable) {
        return paginateByType(searchConditions, user, pageable, StudyGroupFilterType.OWNED);
    }

    /**
     * 로그인한 모든 유저는 조건 별 모든 스터디 그룹 목록을 페이지네이션하여 조회합니다.
     *
     * @param searchConditions 검색 조건
     * @param user             로그인 사용자 정보
     * @param pageable         페이지 정보
     * @return 페이지네이션된 스터디 그룹 목록
     */
    public Page<StudyGroupDto.StudyGroupResponse> paginateAllGroups(
            StudyGroupDto.SearchConditions searchConditions,
            UserDetails user,
            Pageable pageable) {
        return paginateByType(searchConditions, user, pageable, StudyGroupFilterType.ALL);
    }

    /**
     * 로그인한 사용자가 가입한 스터디 그룹 목록을 페이지네이션하여 조회합니다.
     *
     * @param searchConditions 검색 조건
     * @param user             로그인 사용자 정보
     * @param pageable         페이지 정보
     * @return 페이지네이션된 스터디 그룹 목록
     */
    public Page<StudyGroupDto.StudyGroupResponse> paginateMyMemberedGroups(
            StudyGroupDto.SearchConditions searchConditions,
            UserDetails user,
            Pageable pageable) {
        return paginateByType(searchConditions, user, pageable, StudyGroupFilterType.MEMBERED);
    }

    public Optional<StudyGroup> findDetailById(Long groupId) {
        return Optional.ofNullable(queryFactory.selectFrom(studyGroup)
                .leftJoin(studyGroup.owner, user).fetchJoin()
                .leftJoin(studyGroup.categories, groupCategory).fetchJoin()
                .leftJoin(studyGroup.steps, studyStep)
                .leftJoin(studyGroup.studyGroupImage, studyGroupImage).fetchJoin()
                .where(studyGroup.id.eq(groupId))
                .distinct()
                .fetchOne()
        );

    }
}
