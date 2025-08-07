package com.depth.learningcrew.domain.qna.entity;

import java.util.ArrayList;
import java.util.List;

import com.depth.learningcrew.common.auditor.UserStampedEntity;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;

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

  public void addComment(Comment comment) {
    this.comments.add(comment);
    comment.setQAndA(this);
  }

  public void removeComment(Comment comment) {
    this.comments.remove(comment);
    comment.setQAndA(null);
  }
}
