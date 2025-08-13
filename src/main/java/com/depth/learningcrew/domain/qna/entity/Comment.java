package com.depth.learningcrew.domain.qna.entity;

import java.util.ArrayList;
import java.util.List;

import com.depth.learningcrew.common.auditor.UserStampedEntity;
import com.depth.learningcrew.domain.file.entity.CommentAttachedFile;
import com.depth.learningcrew.domain.file.entity.CommentImageFile;
import com.depth.learningcrew.domain.user.entity.Role;
import com.depth.learningcrew.system.exception.model.ErrorCode;
import com.depth.learningcrew.system.exception.model.RestException;
import com.depth.learningcrew.system.security.model.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "COMMENT")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Comment extends UserStampedEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  private Long id;

  @Lob
  @Column(columnDefinition = "LONGTEXT")
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false, name = "q_and_a_id")
  private QAndA qAndA;

  @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<CommentImageFile> attachedImages = new ArrayList<>();

  public void addAttachedImage(CommentImageFile attachedImage) {
    this.attachedImages.add(attachedImage);
    attachedImage.setComment(this);
  }

  public void removeAttachedImage(CommentImageFile attachedImage) {
    this.attachedImages.remove(attachedImage);
    attachedImage.setComment(null);
  }

  @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<CommentAttachedFile> attachedFiles = new ArrayList<>();

  public void addAttachedFile(CommentAttachedFile attachedFile) {
    this.attachedFiles.add(attachedFile);
    attachedFile.setComment(this);
  }

  public void removeAttachedFile(CommentAttachedFile attachedFile) {
    this.attachedFiles.remove(attachedFile);
    attachedFile.setComment(null);
  }

  public void canUpdateBy(UserDetails user) {

    // 관리자는 수정 가능
    if (user.getUser().getRole().equals(Role.ADMIN)) {
      return;
    }

    // 작성자 본인은 수정 가능
    if (this.getCreatedBy() != null && this.getCreatedBy().getId().equals(user.getUser().getId())) {
      return;
    }

    throw new RestException(ErrorCode.AUTH_FORBIDDEN);
  }

  public void canDeleteBy(UserDetails user) {

    // 관리자는 삭제 가능
    if (user.getUser().getRole().equals(Role.ADMIN)) {
      return;
    }

    // 작성자 본인은 삭제 가능
    if (this.getCreatedBy() != null && this.getCreatedBy().getId().equals(user.getUser().getId())) {
      return;
    }

    // 스터디 그룹 주최자는 삭제 가능
    if (this.getQAndA().getStudyGroup().getOwner().getId().equals(user.getUser().getId())) {
      return;
    }

    throw new RestException(ErrorCode.AUTH_FORBIDDEN);
  }

}
