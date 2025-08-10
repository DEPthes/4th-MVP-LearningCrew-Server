package com.depth.learningcrew.domain.note.entity;

import java.util.ArrayList;
import java.util.List;

import com.depth.learningcrew.common.auditor.UserStampedEntity;
import com.depth.learningcrew.domain.file.entity.NoteAttachedFile;
import com.depth.learningcrew.domain.file.entity.NoteImageFile;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.entity.StudyStep;

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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
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
@Table(name = "NOTE")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Note extends UserStampedEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Setter(AccessLevel.NONE)
  private Long id;

  @Column(nullable = false, length = 255)
  private String title;

  @Lob
  @Column(columnDefinition = "LONGTEXT") // 기획팀 설계 상, 한글 1000자 버텨야 함
  private String content;

  @Column(nullable = false)
  private Integer step;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false, name = "study_group_id")
  private StudyGroup studyGroup;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumns({
      @JoinColumn(name = "step", referencedColumnName = "step", insertable = false, updatable = false),
      @JoinColumn(name = "study_group_id", referencedColumnName = "study_group_id", insertable = false, updatable = false)
  })
  private StudyStep studyStep;

  @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<NoteAttachedFile> attachedFiles = new ArrayList<>();

  @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<NoteImageFile> attachedImages = new ArrayList<>();

  public void addAttachedFile(NoteAttachedFile attachedFile) {
    this.attachedFiles.add(attachedFile);
    attachedFile.setNote(this);
  }

  public void addAttachedImage(NoteImageFile attachedImage) {
    this.attachedImages.add(attachedImage);
    attachedImage.setNote(this);
  }

  public void removeAttachedFile(NoteAttachedFile attachedFile) {
    this.attachedFiles.remove(attachedFile);
    attachedFile.setNote(null);
  }

  public void removeAttachedImage(NoteImageFile attachedImage) {
    this.attachedImages.remove(attachedImage);
    attachedImage.setNote(null);
  }

  public void canUpdateBy(User user) {
    // 관리자는 모든 노트를 수정할 수 있음
    if (user.getRole().equals(Role.ADMIN)) {
      return;
    }

    // 작성자는 자신의 노트를 수정할 수 있음
    if (this.getCreatedBy().getId().equals(user.getId())) {
      return;
    }

    // 스터디 그룹 주최자는 그룹 내 노트를 수정할 수 있음
    if (this.studyGroup.getOwner().getId().equals(user.getId())) {
      return;
    }

    throw new RestException(ErrorCode.NOTE_NOT_AUTHORIZED);
  }

}
