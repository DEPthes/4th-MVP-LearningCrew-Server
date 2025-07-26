package com.depth.learningcrew.domain.studygroup.entity;

import com.depth.learningcrew.common.auditor.TimeStampedEntity;
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
}
