package com.depth.learningcrew.domain.studygroup.repository;

import static com.depth.learningcrew.domain.studygroup.entity.QMember.member;
import static com.depth.learningcrew.domain.user.entity.QUser.user;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.depth.learningcrew.domain.studygroup.dto.MemberDto;
import com.depth.learningcrew.domain.studygroup.entity.Member;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.user.entity.User;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {
  private final JPAQueryFactory queryFactory;

  public boolean isMember(StudyGroup studyGroup, User user) {
    return queryFactory
        .selectFrom(member)
        .where(member.id.studyGroup.eq(studyGroup), member.id.user.eq(user))
        .fetchOne() != null;
  }

  public Page<MemberDto.MemberResponse> paginateStudyGroupMembers(
      StudyGroup studyGroup,
      MemberDto.SearchConditions searchConditions,
      Pageable pageable) {

    JPAQuery<Member> contentQuery = buildBaseQuery(studyGroup);
    applySorting(contentQuery, searchConditions);

    List<Member> results = contentQuery
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    Long totalCount = buildCountQuery(studyGroup)
        .fetchOne();

    List<MemberDto.MemberResponse> content = results.stream()
        .map(MemberDto.MemberResponse::from)
        .toList();

    return new PageImpl<>(content, pageable, totalCount != null ? totalCount : 0L);
  }

  private JPAQuery<Member> buildBaseQuery(StudyGroup studyGroup) {
    return queryFactory
        .selectFrom(member)
        .leftJoin(member.id.user, user).fetchJoin()
        .where(
            member.id.studyGroup.eq(studyGroup),
            member.id.user.id.ne(studyGroup.getOwner().getId()));
  }

  private JPAQuery<Long> buildCountQuery(StudyGroup studyGroup) {
    return queryFactory
        .select(member.count())
        .from(member)
        .where(
            member.id.studyGroup.eq(studyGroup),
            member.id.user.id.ne(studyGroup.getOwner().getId()));
  }

  private void applySorting(JPAQuery<Member> query, MemberDto.SearchConditions searchConditions) {
    String sort = searchConditions.getSort() != null ? searchConditions.getSort() : "created_at";
    String order = searchConditions.getOrder() != null ? searchConditions.getOrder() : "desc";

    if ("alphabet".equals(sort)) {
      if ("asc".equals(order)) {
        query.orderBy(member.id.user.nickname.asc());
      } else {
        query.orderBy(member.id.user.nickname.desc());
      }
    } else { // created_at (기본값)
      if ("asc".equals(order)) {
        query.orderBy(member.createdAt.asc());
      } else {
        query.orderBy(member.createdAt.desc());
      }
    }
  }
}