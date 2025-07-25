package com.depth.learningcrew.domain.studygroup.entity;

import com.depth.learningcrew.common.auditor.TimeStampedEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Application extends TimeStampedEntity {

    @EmbeddedId
    private ApplicationId id;

    @Column(nullable = false, length = 40)
    private String state;
}
