package com.depth.learningcrew.domain.studygroup.entity;

import com.depth.learningcrew.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class MemberId implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id", nullable = false)
    private StudyGroup studyGroup;

    public static MemberId of(User user, StudyGroup studyGroup) {
        return MemberId.builder()
                .user(user)
                .studyGroup(studyGroup)
                .build();
    }
}
