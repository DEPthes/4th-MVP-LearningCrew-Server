package com.depth.learningcrew.system.configuration;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// added imports
import com.depth.learningcrew.domain.note.entity.Note;
import com.depth.learningcrew.domain.note.repository.NoteRepository;
import com.depth.learningcrew.domain.studygroup.entity.Application;
import com.depth.learningcrew.domain.studygroup.entity.ApplicationId;
import com.depth.learningcrew.domain.studygroup.entity.GroupCategory;
import com.depth.learningcrew.domain.studygroup.entity.Member;
import com.depth.learningcrew.domain.studygroup.entity.MemberId;
import com.depth.learningcrew.domain.studygroup.entity.State;
import com.depth.learningcrew.domain.studygroup.entity.StudyGroup;
import com.depth.learningcrew.domain.studygroup.entity.StudyStep;
import com.depth.learningcrew.domain.studygroup.entity.StudyStepId;
import com.depth.learningcrew.domain.studygroup.repository.ApplicationRepository;
import com.depth.learningcrew.domain.studygroup.repository.GroupCategoryRepository;
import com.depth.learningcrew.domain.studygroup.repository.MemberRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyGroupRepository;
import com.depth.learningcrew.domain.studygroup.repository.StudyStepRepository;
import com.depth.learningcrew.domain.user.entity.User;
import com.depth.learningcrew.domain.user.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test") // only run in non-test profiles
public class DummyDataInitializer implements ApplicationRunner {

  private final UserRepository userRepository;
  private final StudyGroupRepository studyGroupRepository;
  private final GroupCategoryRepository groupCategoryRepository;
  private final MemberRepository memberRepository;
  private final ApplicationRepository applicationRepository;
  private final StudyStepRepository studyStepRepository;
  private final PasswordEncoder passwordEncoder;
  private final NoteRepository noteRepository; // added

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  @Transactional
  public void run(ApplicationArguments args) throws Exception {
    if (!isDatabaseEmpty()) {
      log.info("Dummy seed skipped: database is not empty.");
      return;
    }

    seedUsers();
    seedGroups();
    log.info("Dummy seed completed.");
  }

  private boolean isDatabaseEmpty() {
    return studyGroupRepository.count() == 0;
  }

  private void seedUsers() throws Exception {
    ClassPathResource usersRes = new ClassPathResource("dummy/users.json");
    try (InputStream in = usersRes.getInputStream()) {
      UsersPayload payload = objectMapper.readValue(in, UsersPayload.class);
      for (UserItem u : payload.getUsers()) {
        if (userRepository.existsByEmail(u.getEmail()))
          continue;
        User toSave = User.builder()
            .email(u.getEmail())
            .password(passwordEncoder.encode(u.getPassword()))
            .nickname(u.getNickname())
            .birthday(LocalDate.parse(u.getBirthday()))
            .gender(u.getGender())
            .build();
        userRepository.save(toSave);
      }
    }
  }

  private void seedGroups() throws Exception {
    ClassPathResource groupsRes = new ClassPathResource("dummy/groups.json");
    try (InputStream in = groupsRes.getInputStream()) {
      GroupsPayload payload = objectMapper.readValue(in, GroupsPayload.class);

      Map<String, User> nickToUser = userRepository.findAll().stream()
          .collect(Collectors.toMap(User::getNickname, it -> it));

      for (GroupItem g : payload.getGroups()) {
        User owner = nickToUser.get(g.getOwnerNickname());
        if (owner == null) {
          log.warn("Owner not found for group: {}", g.getName());
          continue;
        }

        StudyGroup entity = StudyGroup.builder()
            .name(g.getName())
            .summary(g.getSummary())
            .maxMembers(g.getMaxMembers())
            .memberCount(1)
            .currentStep(g.getCurrentStep())
            .startDate(LocalDate.parse(g.getStartDate()))
            .endDate(LocalDate.parse(g.getEndDate()))
            .owner(owner)
            .build();

        // categories
        List<GroupCategory> categories = new ArrayList<>();
        for (String catName : Optional.ofNullable(g.getCategories()).orElseGet(Collections::emptyList)) {
          GroupCategory cat = groupCategoryRepository.findByName(catName)
              .orElseGet(() -> groupCategoryRepository.save(GroupCategory.builder().name(catName).build()));
          categories.add(cat);
        }
        categories.forEach(entity::addCategory);

        entity = studyGroupRepository.save(entity);

        // steps + notes
        for (StepItem s : Optional.ofNullable(g.getSteps()).orElseGet(Collections::emptyList)) {
          StudyStep step = StudyStep.builder()
              .id(StudyStepId.of(s.getStep(), entity))
              .endDate(LocalDate.parse(s.getEndDate()))
              .title(s.getTitle())
              .content(s.getContent())
              .build();
          studyStepRepository.save(step);

          // save notes if present
          for (NoteItem n : Optional.ofNullable(s.getNotes()).orElseGet(Collections::emptyList)) {
            User author = nickToUser.get(n.getAuthorNickname());
            Note note = Note.builder()
                .title(n.getTitle())
                .content(n.getContent())
                .step(s.getStep())
                .studyGroup(entity)
                .build();
            if (author != null) {
              note.setCreatedBy(author);
              note.setLastModifiedBy(author);
            }
            noteRepository.save(note);
          }
        }

        // members
        for (String nick : Optional.ofNullable(g.getMembers()).orElseGet(Collections::emptyList)) {
          User memberUser = nickToUser.get(nick);
          if (memberUser == null)
            continue;
          Member member = Member.builder().id(MemberId.of(memberUser, entity)).build();
          memberRepository.save(member);
          entity.setMemberCount(entity.getMemberCount() + 1);
        }
        studyGroupRepository.save(entity);

        // owner도 멤버로 추가
        Member ownerMember = Member.builder().id(MemberId.of(owner, entity)).build();
        memberRepository.save(ownerMember);
        entity.setMemberCount(entity.getMemberCount() + 1);

        // applications
        for (String nick : Optional.ofNullable(g.getApplications()).orElseGet(Collections::emptyList)) {
          User applicant = nickToUser.get(nick);
          if (applicant == null)
            continue;
          Application app = Application.builder()
              .id(ApplicationId.of(applicant, entity))
              .state(State.PENDING)
              .build();
          applicationRepository.save(app);
        }
      }
    }
  }

  // JSON payload DTOs
  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class UsersPayload {
    List<UserItem> users;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class UserItem {
    private String email;
    private String password;
    private String nickname;
    private String birthday; // yyyy-MM-dd
    private com.depth.learningcrew.domain.user.entity.Gender gender;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class GroupsPayload {
    List<GroupItem> groups;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class GroupItem {
    private String name;
    private String summary;
    private Integer maxMembers;
    private Integer currentStep;
    private String startDate;
    private String endDate;
    private List<String> categories;
    private String ownerNickname;
    private List<String> members;
    private List<String> applications;
    private List<StepItem> steps;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class StepItem {
    private Integer step;
    private String endDate;
    private String title;
    private String content;
    private List<NoteItem> notes; // added
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class NoteItem { // added
    private String authorNickname;
    private String title;
    private String content;
  }
}
