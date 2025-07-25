package com.depth.learningcrew.domain.file.entity;

import com.depth.learningcrew.common.auditor.TimeStampedEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public abstract class AttachedFile extends TimeStampedEntity {

    @Id @Setter(AccessLevel.NONE)
    private String uuid;

    @Enumerated(EnumType.STRING)
    private HandingType handingType;

    private String fileName;
}
