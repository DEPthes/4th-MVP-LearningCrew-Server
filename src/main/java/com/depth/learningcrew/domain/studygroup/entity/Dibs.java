package com.depth.learningcrew.domain.studygroup.entity;

import com.depth.learningcrew.common.auditor.TimeStampedEntity;
import com.depth.learningcrew.domain.studygroup.dto.DibsDto;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Dibs extends TimeStampedEntity {
    @EmbeddedId
    private DibsId id;

    public static Dibs from(DibsId dibsId) {
        return Dibs.builder()
                .id(dibsId)
                .build();
    }
}
