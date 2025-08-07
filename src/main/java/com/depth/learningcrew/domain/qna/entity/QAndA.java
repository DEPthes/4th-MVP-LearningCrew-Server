package com.depth.learningcrew.domain.qna.entity;

import java.util.ArrayList;
import java.util.List;

import com.depth.learningcrew.common.auditor.UserStampedEntity;
import com.depth.learningcrew.domain.file.entity.QAndAAttachedFile;
import com.depth.learningcrew.domain.file.entity.QAndAImageFile;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "Q_AND_A")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class QAndA extends UserStampedEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  private Long id;

  @Column(nullable = false)
  private Integer step;

  @Column(nullable = false, length = 255)
  private String title;

  @Lob
  @Column
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  private StudyGroup studyGroup;

  @OneToMany(mappedBy = "qAndA", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Comment> comments = new ArrayList<>();

  @OneToMany(mappedBy = "qAndA", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<QAndAAttachedFile> attachedFiles = new ArrayList<>();

  @OneToMany(mappedBy = "qAndA", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<QAndAImageFile> attachedImages = new ArrayList<>();

  public void addComment(Comment comment) {
    this.comments.add(comment);
    comment.setQAndA(this);
  }

  public void removeComment(Comment comment) {
    this.comments.remove(comment);
    comment.setQAndA(null);
  }

  public void addAttachedFile(QAndAAttachedFile attachedFile) {
    this.attachedFiles.add(attachedFile);
    attachedFile.setQAndA(this);
  }

  public void addAttachedImage(QAndAImageFile attachedImage) {
    this.attachedImages.add(attachedImage);
    attachedImage.setQAndA(this);
  }

  public void removeAttachedFile(QAndAAttachedFile attachedFile) {
    this.attachedFiles.remove(attachedFile);
    attachedFile.setQAndA(null);
  }

  public void removeAttachedImage(QAndAImageFile attachedImage) {
    this.attachedImages.remove(attachedImage);
    attachedImage.setQAndA(null);
  }

  public void canUpdateBy(User user) {
    // 관리자는 모든 질문을 수정할 수 있음
    if (user.getRole().equals(Role.ADMIN)) {
      return;
    }

    // 질문 작성자는 자신의 질문을 수정할 수 있음
    if (this.getCreatedBy().getId().equals(user.getId())) {
      return;
    }

    // 스터디 그룹 주최자는 그룹 내 질문을 수정할 수 있음
    if (this.studyGroup.getOwner().getId().equals(user.getId())) {
      return;
    }

    throw new RestException(ErrorCode.QANDA_NOT_AUTHORIZED);
  }
}
