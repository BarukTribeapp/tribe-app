package com.tribe.app;

import jakarta.persistence.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class TribeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TribeApplication.class, args);
    }

    @Bean
    DataSource dataSource(Environment env) {
        String databaseUrl = env.getProperty("DATABASE_URL");
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");

        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            URI uri = URI.create(databaseUrl);
            String[] userInfo = uri.getUserInfo().split(":");
            String username = userInfo[0];
            String password = userInfo.length > 1 ? userInfo[1] : "";
            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath();

            ds.setUrl(jdbcUrl);
            ds.setUsername(username);
            ds.setPassword(password);
        } else {
            String host = env.getProperty("DB_HOST", "localhost");
            String port = env.getProperty("DB_PORT", "5432");
            String db = env.getProperty("DB_NAME", "tribe_db");
            String user = env.getProperty("DB_USER", "tribe_user");
            String pass = env.getProperty("DB_PASSWORD", "tribe_pass");

            ds.setUrl("jdbc:postgresql://" + host + ":" + port + "/" + db);
            ds.setUsername(user);
            ds.setPassword(pass);
        }

        return ds;
    }

    @Bean
    CommandLineRunner seed(UserRepository users, LessonRepository lessons, LessonProgressRepository progresses) {
        return args -> {
            if (users.count() == 0) {
                UserAccount arthur = new UserAccount();
                arthur.setUsername("Arthur");
                arthur.setTotalXp(0);
                arthur.setStreakDays(1);
                arthur.setGems(120);
                arthur.setLives(5);
                arthur.setCompletedLessons(0);
                arthur.setClaimedMissionIds("");
                arthur.setLastStudyDate(LocalDate.now());
                users.save(arthur);

                UserAccount bianca = new UserAccount();
                bianca.setUsername("Bianca");
                bianca.setTotalXp(90);
                bianca.setStreakDays(4);
                bianca.setGems(180);
                bianca.setLives(5);
                bianca.setCompletedLessons(3);
                bianca.setClaimedMissionIds("");
                bianca.setLastStudyDate(LocalDate.now());
                users.save(bianca);

                UserAccount caio = new UserAccount();
                caio.setUsername("Caio");
                caio.setTotalXp(150);
                caio.setStreakDays(5);
                caio.setGems(220);
                caio.setLives(5);
                caio.setCompletedLessons(4);
                caio.setClaimedMissionIds("");
                caio.setLastStudyDate(LocalDate.now());
                users.save(caio);
            }

            if (lessons.count() == 0) {
                Lesson l1 = new Lesson();
                l1.setTitle("Saudações");
                l1.setXpReward(10);
                l1.setQuestionText("Como se diz 'Olá' em inglês?");
                l1.setCorrectAnswer("Hello");
                l1.setOptions(List.of("Hello", "Please", "Night", "Thanks"));
                lessons.save(l1);

                Lesson l2 = new Lesson();
                l2.setTitle("Vocabulário básico");
                l2.setXpReward(15);
                l2.setQuestionText("Qual palavra significa 'água'?");
                l2.setCorrectAnswer("Water");
                l2.setOptions(List.of("Rice", "Water", "Coffee", "Bread"));
                lessons.save(l2);

                Lesson l3 = new Lesson();
                l3.setTitle("Rotina");
                l3.setXpReward(20);
                l3.setQuestionText("Qual frase significa 'Eu estudo todos os dias'?");
                l3.setCorrectAnswer("I study every day");
                l3.setOptions(List.of(
                        "I sleep every day",
                        "I study every day",
                        "I eat every day",
                        "I walk every day"
                ));
                lessons.save(l3);
            }
        };
    }
}

/* =========================
   ENTIDADES
   ========================= */

@Entity
@Table(name = "users")
class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private Integer totalXp;
    private Integer streakDays;
    private Integer gems;
    private Integer lives;
    private Integer completedLessons;

    @Column(length = 1000)
    private String claimedMissionIds;

    private LocalDate lastStudyDate;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public Integer getTotalXp() { return totalXp; }
    public Integer getStreakDays() { return streakDays; }
    public Integer getGems() { return gems; }
    public Integer getLives() { return lives; }
    public Integer getCompletedLessons() { return completedLessons; }
    public String getClaimedMissionIds() { return claimedMissionIds; }
    public LocalDate getLastStudyDate() { return lastStudyDate; }

    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setTotalXp(Integer totalXp) { this.totalXp = totalXp; }
    public void setStreakDays(Integer streakDays) { this.streakDays = streakDays; }
    public void setGems(Integer gems) { this.gems = gems; }
    public void setLives(Integer lives) { this.lives = lives; }
    public void setCompletedLessons(Integer completedLessons) { this.completedLessons = completedLessons; }
    public void setClaimedMissionIds(String claimedMissionIds) { this.claimedMissionIds = claimedMissionIds; }
    public void setLastStudyDate(LocalDate lastStudyDate) { this.lastStudyDate = lastStudyDate; }
}

@Entity
@Table(name = "lessons")
class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Integer xpReward;

    @Column(length = 500)
    private String questionText;

    private String correctAnswer;

    @ElementCollection
    @CollectionTable(name = "lesson_options", joinColumns = @JoinColumn(name = "lesson_id"))
    @Column(name = "option_value")
    private List<String> options = new ArrayList<>();

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Integer getXpReward() { return xpReward; }
    public String getQuestionText() { return questionText; }
    public String getCorrectAnswer() { return correctAnswer; }
    public List<String> getOptions() { return options; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setXpReward(Integer xpReward) { this.xpReward = xpReward; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public void setOptions(List<String> options) { this.options = options; }
}

@Entity
@Table(name = "lesson_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "lesson_id"}))
class LessonProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private UserAccount user;

    @ManyToOne(optional = false)
    private Lesson lesson;

    private Integer attempts;
    private Integer earnedXp;
    private Boolean completed;

    public Long getId() { return id; }
    public UserAccount getUser() { return user; }
    public Lesson getLesson() { return lesson; }
    public Integer getAttempts() { return attempts; }
    public Integer getEarnedXp() { return earnedXp; }
    public Boolean getCompleted() { return completed; }

    public void setId(Long id) { this.id = id; }
    public void setUser(UserAccount user) { this.user = user; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }
    public void setEarnedXp(Integer earnedXp) { this.earnedXp = earnedXp; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
}

/* =========================
   REPOSITÓRIOS
   ========================= */

interface UserRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
}

interface LessonRepository extends JpaRepository<Lesson, Long> {
}

interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByUserAndLesson(UserAccount user, Lesson lesson);
    List<LessonProgress> findByUser(UserAccount user);
}

/* =========================
   API
   ========================= */

@RestController
@RequestMapping("/api")
class ApiController {

    private final UserRepository users;
    private final LessonRepository lessons;
    private final LessonProgressRepository progresses;

    ApiController(UserRepository users, LessonRepository lessons, LessonProgressRepository progresses) {
        this.users = users;
        this.lessons = lessons;
        this.progresses = progresses;
    }

    private UserAccount currentUser() {
        return users.findByUsername("Arthur").orElseThrow();
    }

    @GetMapping("/health")
    Map<String, Object> health() {
        return Map.of("ok", true, "service", "TRIBE");
    }

    @GetMapping("/me")
    Map<String, Object> me() {
        return toUserMap(currentUser());
    }

    @GetMapping("/lessons")
    List<Map<String, Object>> getLessons() {
        return lessons.findAll().stream()
                .map(lesson -> Map.of(
                        "id", lesson.getId(),
                        "title", lesson.getTitle(),
                        "xpReward", lesson.getXpReward(),
                        "questionText", lesson.getQuestionText(),
                        "correctAnswer", lesson.getCorrectAnswer(),
                        "options", lesson.getOptions()
                ))
                .toList();
    }

    @GetMapping("/progress")
    List<Map<String, Object>> getProgress() {
        UserAccount user = currentUser();
        return progresses.findByUser(user).stream()
                .map(p -> Map.of(
                        "lessonId", p.getLesson().getId(),
                        "attempts", p.getAttempts(),
                        "earnedXp", p.getEarnedXp(),
                        "completed", p.getCompleted()
                ))
                .toList();
    }

    @PostMapping("/answer")
    Map<String, Object> answer(@RequestBody Map<String, Object> body) {
        UserAccount user = currentUser();
        Long lessonId = Long.valueOf(body.get("lessonId").toString());
        String selectedOption = Objects.toString(body.get("selectedOption"), "");

        Lesson lesson = lessons.findById(lessonId).orElseThrow();
        boolean correct = lesson.getCorrectAnswer().equals(selectedOption);

        LessonProgress progress = progresses.findByUserAndLesson(user, lesson).orElseGet(() -> {
            LessonProgress p = new LessonProgress();
            p.setUser(user);
            p.setLesson(lesson);
            p.setAttempts(0);
            p.setEarnedXp(0);
            p.setCompleted(false);
            return p;
        });

        progress.setAttempts(progress.getAttempts() + 1);

        if (correct) {
            if (!Boolean.TRUE.equals(progress.getCompleted())) {
                progress.setCompleted(true);
                progress.setEarnedXp(lesson.getXpReward());

                user.setTotalXp(user.getTotalXp() + lesson.getXpReward());
                user.setCompletedLessons(user.getCompletedLessons() + 1);
                user.setGems(user.getGems() + 20);
                user.setLives(Math.min(5, user.getLives() + 1));

                LocalDate today = LocalDate.now();
                LocalDate last = user.getLastStudyDate();
                if (last == null) {
                    user.setStreakDays(1);
                } else if (last.equals(today)) {
                    // mantém
                } else if (last.plusDays(1).equals(today)) {
                    user.setStreakDays(user.getStreakDays() + 1);
                } else {
                    user.setStreakDays(1);
                }
                user.setLastStudyDate(today);
                users.save(user);
            }
        } else {
            user.setLives(Math.max(0, user.getLives() - 1));
            users.save(user);
        }

        progresses.save(progress);

        return Map.of(
                "correct", correct,
                "message", correct ? "Resposta correta!" : "Resposta incorreta.",
                "user", toUserMap(currentUser())
        );
    }

    @GetMapping("/ranking")
    List<Map<String, Object>> ranking() {
        return users.findAll().stream()
                .sorted((a, b) -> Integer.compare(b.getTotalXp(), a.getTotalXp()))
                .map(u -> Map.of(
                        "username", u.getUsername(),
                        "totalXp", u.getTotalXp()
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/missions")
    List<Map<String, Object>> missions() {
        UserAccount user = currentUser();
        Set<String> claimed = claimedSet(user);

        List<Map<String, Object>> list = new ArrayList<>();
        list.add(mission("xp20", "Ganhe 20 XP", user.getTotalXp(), 20, 15, claimed));
        list.add(mission("lesson1", "Complete 1 lição", user.getCompletedLessons(), 1, 20, claimed));
        list.add(mission("streak3", "Alcance ofensiva 3", user.getStreakDays(), 3, 30, claimed));
        return list;
    }

    @PostMapping("/missions/{id}/claim")
    Map<String, Object> claimMission(@PathVariable String id) {
        UserAccount user = currentUser();
        Set<String> claimed = claimedSet(user);

        if (claimed.contains(id)) {
            return Map.of("ok", false, "message", "Missão já resgatada.");
        }

        Map<String, Map<String, Integer>> rules = Map.of(
                "xp20", Map.of("current", user.getTotalXp(), "target", 20, "reward", 15),
                "lesson1", Map.of("current", user.getCompletedLessons(), "target", 1, "reward", 20),
                "streak3", Map.of("current", user.getStreakDays(), "target", 3, "reward", 30)
        );

        if (!rules.containsKey(id)) {
            return Map.of("ok", false, "message", "Missão não encontrada.");
        }

        Map<String, Integer> rule = rules.get(id);
        if (rule.get("current") < rule.get("target")) {
            return Map.of("ok", false, "message", "Missão ainda não concluída.");
        }

        claimed.add(id);
        user.setClaimedMissionIds(String.join(",", claimed));
        user.setGems(user.getGems() + rule.get("reward"));
        users.save(user);

        return Map.of("ok", true, "message", "Missão resgatada!", "user", toUserMap(user));
    }

    @GetMapping("/shop")
    List<Map<String, Object>> shop() {
        return List.of(
                Map.of("id", "lives", "title", "Recarga de vidas", "price", 80, "description", "Restaura todas as vidas"),
                Map.of("id", "bonus", "title", "Bônus de gems", "price", 60, "description", "Compra 40 gems extras"),
                Map.of("id", "streak", "title", "Escudo de ofensiva", "price", 120, "description", "Aumenta sua ofensiva em +1")
        );
    }

    @PostMapping("/shop/{id}/buy")
    Map<String, Object> buy(@PathVariable String id) {
        UserAccount user = currentUser();

        int price;
        switch (id) {
            case "lives" -> price = 80;
            case "bonus" -> price = 60;
            case "streak" -> price = 120;
            default -> {
                return Map.of("ok", false, "message", "Item não encontrado.");
            }
        }

        if (user.getGems() < price) {
            return Map.of("ok", false, "message", "Gems insuficientes.");
        }

        user.setGems(user.getGems() - price);

        switch (id) {
            case "lives" -> user.setLives(5);
            case "bonus" -> user.setGems(user.getGems() + 40);
            case "streak" -> user.setStreakDays(user.getStreakDays() + 1);
        }

        users.save(user);
        return Map.of("ok", true, "message", "Compra realizada.", "user", toUserMap(user));
    }

    private Map<String, Object> mission(String id, String title, int progress, int target, int reward, Set<String> claimed) {
        return Map.of(
                "id", id,
                "title", title,
                "progress", progress,
                "target", target,
                "rewardGems", reward,
                "completed", progress >= target,
                "claimed", claimed.contains(id)
        );
    }

    private Set<String> claimedSet(UserAccount user) {
        if (user.getClaimedMissionIds() == null || user.getClaimedMissionIds().isBlank()) {
            return new LinkedHashSet<>();
        }
        return Arrays.stream(user.getClaimedMissionIds().split(","))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<String, Object> toUserMap(UserAccount user) {
        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "totalXp", user.getTotalXp(),
                "streakDays", user.getStreakDays(),
                "gems", user.getGems(),
                "lives", user.getLives(),
                "completedLessons", user.getCompletedLessons()
        );
    }
}
