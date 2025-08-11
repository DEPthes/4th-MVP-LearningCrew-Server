package com.depth.learningcrew.domain.note.repository;

import com.depth.learningcrew.domain.note.dto.NoteDto;
import com.depth.learningcrew.domain.note.entity.Note;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.depth.learningcrew.domain.note.entity.QNote.note;

@Repository
@RequiredArgsConstructor
public class NoteQueryRepository {
    private final JPAQueryFactory queryFactory;

    public List<Note> findSharedNotes(StudyGroup studyGroup,
                                      Integer step,
                                      Long excludeUserId,
                                      NoteDto.SearchConditions searchConditions) {

        JPAQuery<Note> query = queryFactory
                .selectFrom(note)
                .leftJoin(note.createdBy).fetchJoin()
                .where(
                        note.studyGroup.eq(studyGroup),
                        note.step.eq(step),
                        note.createdBy.id.ne(excludeUserId) // 내 노트 제외
                );

        applySorting(query, searchConditions);
        return query.limit(20).fetch();
    }

    private void applySorting(JPAQuery<Note> query, NoteDto.SearchConditions searchConditions) {
        String sort  = (searchConditions != null && searchConditions.getSort()  != null)
                ? searchConditions.getSort() : "created_at";
        String order = (searchConditions != null && searchConditions.getOrder() != null)
                ? searchConditions.getOrder() : "desc";

        if ("alphabet".equals(sort)) {
            if ("asc".equals(order)) {
                query.orderBy(note.title.asc());
            } else {
                query.orderBy(note.title.desc());
            }
        } else {
            if ("asc".equals(order)) {
                query.orderBy(note.createdAt.asc());
            } else {
                query.orderBy(note.createdAt.desc());
            }
        }
    }
}