package com.depth.learningcrew.domain.studygroup.entity;

import java.time.LocalDateTime;

import com.depth.learningcrew.common.auditor.TimeStampedEntity;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Application extends TimeStampedEntity {

    @EmbeddedId
    private ApplicationId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private State state;

    @Column
    private LocalDateTime approvedAt;

    public void approve() {
        if (this.state == State.APPROVED) {
            throw new RestException(ErrorCode.STUDY_GROUP_APPLICATION_ALREADY_APPROVED);
        }
        this.state = State.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject() {
        if (this.state == State.REJECTED) {
            throw new RestException(ErrorCode.STUDY_GROUP_APPLICATION_ALREADY_REJECTED);
        }
        this.state = State.REJECTED;
        this.approvedAt = LocalDateTime.now();
    }

    public void canApprovedBy(UserDetails userDetails) {
        if (!this.id.getStudyGroup().getOwner().getId().equals(userDetails.getUser().getId())) {
            throw new RestException(ErrorCode.AUTH_FORBIDDEN);
        }
    }

    public void canRejectBy(UserDetails userDetails) {
        if (!this.id.getStudyGroup().getOwner().getId().equals(userDetails.getUser().getId())) {
            throw new RestException(ErrorCode.AUTH_FORBIDDEN);
        }
    }

    public void canApproveNow() {
        // 이미 승인된 신청은 승인할 수 없음
        if (this.state == State.APPROVED) {
            throw new RestException(ErrorCode.STUDY_GROUP_APPLICATION_ALREADY_APPROVED);
        }

        // 신청이 거절된 경우 승인할 수 없음
        if (this.state == State.REJECTED) {
            throw new RestException(ErrorCode.STUDY_GROUP_APPLICATION_ALREADY_REJECTED);
        }
    }

    public void canRejectNow() {
        // 이미 승인된 신청은 거절할 수 없음
        if (this.state == State.APPROVED) {
            throw new RestException(ErrorCode.STUDY_GROUP_APPLICATION_ALREADY_APPROVED);
        }

        // 이미 거절된 신청은 거절할 수 없음
        if (this.state == State.REJECTED) {
            throw new RestException(ErrorCode.STUDY_GROUP_APPLICATION_ALREADY_REJECTED);
        }
    }
}
