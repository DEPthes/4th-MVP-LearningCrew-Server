package com.depth.learningcrew.system.configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.depth.learningcrew.domain.file.entity.ProfileImage;
import com.depth.learningcrew.domain.file.entity.StudyGroupImage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
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
import org.springframework.web.multipart.MultipartFile;

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

  @Value("${file.save-path:./upload}")
  private String fileSavePath;

  @PersistenceContext
  private EntityManager em;

  @Override
  @Transactional
  public void run(ApplicationArguments args) throws Exception {
    if (!isDatabaseEmpty()) {
      log.info("Dummy seed skipped: database is not empty.");
      return;
    }

    seedUsers();
    seedGroups();
    seedImages();

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

  /**
   * 이미지 더미 등록:
   * - users: 프로필 이미지
   * - groups: 스터디 그룹 대표 이미지
   * - steps, notes 디렉토리는 이번 단계에서 무시 (요구사항)
   */
  private void seedImages() throws IOException {
    ensureSaveDir();

    seedUserProfileImages();
    seedStudyGroupImages();
    seedNoteImages();

    log.info("Dummy image seed done.");
  }

  private void seedUserProfileImages() throws IOException {
    Resource usersDir = new ClassPathResource("dummy/images/users");
    if (!usersDir.exists()) {
      log.warn("No user images directory found: {}", usersDir);
      return;
    }

    Map<String, String> emailByBaseName = Map.of(
            "study_usera", "study_usera@example.com",
            "study_userb", "study_userb@example.com",
            "study_userc", "study_userc@example.com",
            "study_userd", "study_userd@example.com",
            "study_usere", "study_usere@example.com",
            "study_userf", "study_userf@example.com"
    );

    for (String base : emailByBaseName.keySet()) {
      String fileName = base + ".jpg";
      ClassPathResource fileRes = new ClassPathResource("dummy/images/users/" + fileName);
      if (!fileRes.exists()) {
        log.warn("User image not found for {} -> {}", base, fileName);
        continue;
      }
      byte[] bytes;
      try (InputStream in = fileRes.getInputStream()) {
        bytes = in.readAllBytes();
      }

      String email = emailByBaseName.get(base);
      userRepository.findByEmail(email).ifPresentOrElse(user -> {
        try {
          MultipartFile mf = new BytesMultipartFile("file", fileName, bytes);
          ProfileImage profileImage = ProfileImage.from(mf); // uuid/size/fileName/handlingType 설정
          if (profileImage == null) {
            log.warn("ProfileImage.from returned null for {}", fileName);
            return;
          }
          profileImage.setUser(user);
          // 양방향 편의설정
          user.setProfileImage(profileImage);

          // 파일 저장 (uuid로 저장)
          writeToDisk(profileImage.getUuid(), bytes);

          // ProfileImage는 소유측(owning)이므로 profileImage를 persist
          em.persist(profileImage);
          em.flush();

          log.info("Seeded profile image for user {} -> {}", email, fileName);
        } catch (Exception e) {
          log.error("Failed to seed profile image for {}: {}", email, e.getMessage(), e);
        }
      }, () -> log.warn("User not found for email {}", email));
    }
  }

  private void seedStudyGroupImages() throws IOException {
    // 미리 모든 그룹을 조회하여 이름 → 엔티티 매핑
    Map<String, StudyGroup> byName = studyGroupRepository.findAll().stream()
            .collect(Collectors.toMap(StudyGroup::getName, g -> g, (a,b) -> a));

    Map<String, String> groupNameByCode = Map.of(
            "g_group.jpg", "일본어 기초부터 실전까지",
            "h_group.jpg", "기획 첫걸음",
            "i_group.jpg", "타이포그래피 공부"
    );

    for (var e : groupNameByCode.entrySet()) {
      String fileName = e.getKey();
      String groupName = e.getValue();

      ClassPathResource fileRes = new ClassPathResource("dummy/images/groups/" + fileName);
      if (!fileRes.exists()) {
        log.warn("Group image not found for {}", fileName);
        continue;
      }

      StudyGroup group = byName.get(groupName);
      if (group == null) {
        log.warn("StudyGroup not found for name {}", groupName);
        continue;
      }

      byte[] bytes;
      try (InputStream in = fileRes.getInputStream()) {
        bytes = in.readAllBytes();
      }

      try {
        MultipartFile mf = new BytesMultipartFile("file", fileName, bytes);
        StudyGroupImage sgi = StudyGroupImage.from(mf, group); // 소유측 엔티티
        group.setStudyGroupImage(sgi);                          // 양방향 세팅

        writeToDisk(sgi.getUuid(), bytes);                      // 실제 파일 저장
        em.persist(sgi);                                        // 명시적 persist (안전)

        log.info("Seeded study group image for '{}' -> {}", groupName, fileName);
      } catch (Exception ex) {
        log.error("Failed to seed study group image for '{}': {}", groupName, ex.getMessage(), ex);
      }
    }
  }

  private void seedNoteImages() throws IOException {
    // 1) 그룹 이름 캐시
    Map<String, StudyGroup> groupByName = studyGroupRepository.findAll().stream()
            .collect(Collectors.toMap(StudyGroup::getName, g -> g, (a,b)->a));

    // 2) 그룹 코드 → 이름
    Map<String, String> groupCodeToName = Map.of(
            "g", "일본어 기초부터 실전까지",
            "h", "기획 첫걸음",
            "i", "타이포그래피 공부"
    );

    // 3) user코드 → 닉네임 (users.json 기준)
    Map<String, String> userCodeToNickname = Map.of(
            "a", "김하린",
            "b", "박지훈",
            "c", "최현우",
            "d", "이지우",
            "e", "윤민석",
            "f", "박서연"
    );

    // 4) 모든 노트 미리 로드(관리 상태), 작성자/그룹/스텝 기준으로 조회하기 쉽게 인메모리 인덱스 구성
    List<Note> allNotes = noteRepository.findAll();
    // Map<groupId, Map<step, Map<authorUserId, List<Note>>>> 형태
    Map<Long, Map<Integer, Map<Long, List<Note>>>> noteIndex = allNotes.stream()
            .collect(Collectors.groupingBy(n -> n.getStudyGroup().getId(),
                    Collectors.groupingBy(Note::getStep,
                            Collectors.groupingBy(n -> Optional.ofNullable(n.getCreatedBy()).map(User::getId).orElse(-1L)))));

    // 5) 닉네임 → UserId 매핑(작성자 식별)
    Map<String, Long> nickToUserId = userRepository.findAll().stream()
            .collect(Collectors.toMap(User::getNickname, User::getId, (a,b)->a));

    // 6) notes 디렉터리 순회
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    org.springframework.core.io.Resource[] resources =
            resolver.getResources("classpath*:dummy/images/notes/*.jpg");

    if (resources.length == 0) {
      log.info("No note images found under dummy/images/notes");
      return;
    }

    // 파일명 파서
    java.util.regex.Pattern p = java.util.regex.Pattern.compile("^([ghi])_group_step(\\d+)_user([a-f])_note_(\\d+)\\.jpg$");

    for (org.springframework.core.io.Resource res : resources) {
      String fileName = res.getFilename();
      if (fileName == null) continue;

      var m = p.matcher(fileName);
      if (!m.matches()) {
        log.warn("Skip note image with unrecognized name: {}", fileName);
        continue;
      }

      String groupCode = m.group(1);                   // g/h/i
      int step = Integer.parseInt(m.group(2));         // step number
      String userCode = m.group(3);                    // a~f
      int nth = Integer.parseInt(m.group(4));          // 1-based index

      String groupName = groupCodeToName.get(groupCode);
      if (groupName == null) {
        log.warn("Unknown group code '{}' for file {}", groupCode, fileName);
        continue;
      }

      StudyGroup group = groupByName.get(groupName);
      if (group == null) {
        log.warn("StudyGroup not found for name '{}' (file: {})", groupName, fileName);
        continue;
      }

      String authorNickname = userCodeToNickname.get(userCode);
      Long authorId = nickToUserId.get(authorNickname);
      if (authorId == null) {
        log.warn("Author not found for nickname '{}' (file: {})", authorNickname, fileName);
        continue;
      }

      // 해당 그룹/스텝/작성자의 노트 목록(오름차순 정렬)
      List<Note> notes = Optional.ofNullable(
                      Optional.ofNullable(noteIndex.get(group.getId()))
                              .map(byStep -> byStep.get(step)).orElse(null))
              .map(byAuthor -> byAuthor.get(authorId))
              .orElseGet(Collections::emptyList)
              .stream()
              .sorted(Comparator.comparing(Note::getId))
              .toList();

      if (notes.isEmpty()) {
        log.warn("No notes found for group='{}', step={}, author='{}'. File={}",
                groupName, step, authorNickname, fileName);
        continue; // 노트가 없으면 스킵 (임의 생성 X)
      }

      // 이미지가 붙을 대상 노트 = 해당 작성자의 해당 스텝 노트 중 '첫 번째'
      Note targetNote = notes.get(0);

      // 바이트 로드
      byte[] bytes;
      try (InputStream in = res.getInputStream()) {
        bytes = in.readAllBytes();
      } catch (IOException e) {
        log.error("Failed to read note image {}: {}", fileName, e.getMessage(), e);
        continue;
      }

      try {
        MultipartFile mf = new BytesMultipartFile("file", fileName, bytes);
        var noteImg = com.depth.learningcrew.domain.file.entity.NoteImageFile.from(mf);
        if (noteImg == null) {
          log.warn("NoteImageFile.from returned null for {}", fileName);
          continue;
        }

        // 동일 노트에 n번째 이미지로 추가(정렬 필드는 없지만, 생성 순서대로 누적)
        targetNote.addAttachedImage(noteImg);

        // 디스크 저장
        writeToDisk(noteImg.getUuid(), bytes);

        // 명시적 저장(안전)
        em.persist(noteImg);

        log.info("Seeded note image: file='{}' -> noteId={}, group='{}', step={}, author='{}' (image #{})",
                fileName, targetNote.getId(), groupName, step, authorNickname, nth);

      } catch (Exception ex) {
        log.error("Failed to seed note image for file {}: {}", fileName, ex.getMessage(), ex);
      }
    }
  }

  private void ensureSaveDir() throws IOException {
    Path p = Path.of(fileSavePath);
    if (!Files.exists(p)) {
      Files.createDirectories(p);
    }
  }

  private void writeToDisk(String uuid, byte[] bytes) throws IOException {
    // 실제 업로드 저장과 유사하게 uuid 파일명으로 저장
    Path target = Path.of(fileSavePath, uuid);
    Files.write(target, bytes);
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

  /**
   * 간단한 MultipartFile 구현체 (클래스패스 리소스를 from() 팩토리에 태우기 위한 용도)
   */
  private static class BytesMultipartFile implements MultipartFile {
    private final String name;
    private final String originalFilename;
    private final byte[] content;

    private BytesMultipartFile(String name, String originalFilename, byte[] content) {
      this.name = name;
      this.originalFilename = originalFilename;
      this.content = (content != null ? content : new byte[0]);
    }

    @Override public String getName() { return name; }
    @Override public String getOriginalFilename() { return originalFilename; }
    @Override public String getContentType() { return null; }
    @Override public boolean isEmpty() { return content.length == 0; }
    @Override public long getSize() { return content.length; }
    @Override public byte[] getBytes() { return content; }
    @Override public InputStream getInputStream() { return new ByteArrayInputStream(content); }
    @Override public void transferTo(java.io.File dest) throws IOException { Files.write(dest.toPath(), content); }
  }
}
