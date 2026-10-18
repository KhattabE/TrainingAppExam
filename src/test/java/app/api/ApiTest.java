package app.api;

import app.dao.ExerciseDAOImpl;
import app.dao.UserDAOImpl;
import app.dao.WorkoutProgramDAOImpl;
import app.dto.ExerciseRequestDTO;
import app.dto.GeneratedExerciseDTO;
import app.dto.GeneratedWorkoutProgramDTO;
import app.dto.LoginRequestDTO;
import app.dto.RegisterRequestDTO;
import app.dto.UserRequestDTO;
import app.entities.ExperienceLevel;
import app.entities.MuscleGroup;
import app.entities.TrainingGoal;
import app.entities.User;
import app.entities.WorkoutProgram;
import app.testutils.DatabaseTest;
import app.testutils.FakeAiCoachService;
import app.services.JwtService;
import app.services.PasswordService;
import io.javalin.Javalin;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class ApiTest extends DatabaseTest {

    private static final String TEST_SECRET = "test-secret-key-that-is-at-least-32-bytes-long";
    private final FakeAiCoachService ai = new FakeAiCoachService();
    private final JwtService jwtService = new JwtService(TEST_SECRET);
    private Javalin app;

    @BeforeAll
    void startApi() {
        app = Application.createApp(emf, ai, jwtService).start(0);
    }

    @BeforeEach
    void resetAi() {
        ai.lastPrompt = null;
        ai.lastUser = null;
        ai.program = validProgram();
    }

    @AfterAll
    void stopApi() {
        if (app != null) {
            app.stop();
        }
    }

    private RequestSpecification request() {
        return given().baseUri("http://localhost").port(app.port()).contentType(ContentType.JSON);
    }

    @Test
    void healthReturnsJson() {
        request().get("/api/health").then().statusCode(200).contentType(ContentType.JSON)
                .body("status", equalTo("ok"), "message", equalTo("API is running"));
    }

    @Test
    void listExercisesReturnsSeededDtos() {
        request().get("/api/exercises").then().statusCode(200).contentType(ContentType.JSON)
                .body("id", containsInAnyOrder(seed.benchPress().getId(), seed.squat().getId()),
                        "name", containsInAnyOrder("Bench Press", "Squat"));
    }

    @Test
    void getExerciseReturnsItsFields() {
        request().get("/api/exercises/" + seed.benchPress().getId()).then().statusCode(200)
                .body("id", equalTo(seed.benchPress().getId()), "name", equalTo("Bench Press"),
                        "muscleGroup", equalTo("CHEST"), "sets", equalTo(3), "reps", equalTo(10));
    }

    @Test
    void createExercisePersistsAndCanBeRead() {
        ExerciseRequestDTO exercise = new ExerciseRequestDTO("Curl", MuscleGroup.BICEPS, 3, 12);
        int id = request().body(exercise).post("/api/exercises").then().statusCode(201)
                .body("id", greaterThan(0), "name", equalTo("Curl"), "muscleGroup", equalTo("BICEPS"))
                .extract().path("id");
        request().get("/api/exercises/" + id).then().statusCode(200).body("sets", equalTo(3), "reps", equalTo(12));
        assertEquals("Curl", new ExerciseDAOImpl(emf).getById(id).getName());
    }

    @Test
    void updateExercisePersistsAllChangedFields() {
        ExerciseRequestDTO exercise = new ExerciseRequestDTO("Incline Bench Press", MuscleGroup.CHEST, 4, 8);
        int id = seed.benchPress().getId();
        request().body(exercise).put("/api/exercises/" + id).then().statusCode(200)
                .body("id", equalTo(id), "name", equalTo("Incline Bench Press"), "sets", equalTo(4), "reps", equalTo(8));
        assertEquals(4, new ExerciseDAOImpl(emf).getById(id).getSets());
    }

    @Test
    void deleteExerciseReturnsEmptyBodyAndRemovesRecord() {
        int id = request().body(new ExerciseRequestDTO("Curl", MuscleGroup.BICEPS, 3, 12))
                .post("/api/exercises").then().statusCode(201).extract().path("id");
        request().delete("/api/exercises/" + id).then().statusCode(204).body(isEmptyString());
        assertNull(new ExerciseDAOImpl(emf).getById(id));
        request().get("/api/exercises/" + id).then().statusCode(404).body("status", equalTo(404));
    }

    @Test
    void listUsersReturnsSeededDtosAndProgramId() {
        request().get("/api/users").then().statusCode(200).contentType(ContentType.JSON)
                .body("id", containsInAnyOrder(seed.beginner().getId(), seed.advanced().getId()),
                        "find { it.name == 'Anna' }.workoutProgramId", equalTo(seed.fullBody().getId()),
                        "find { it.name == 'Omar' }.workoutProgramId", nullValue());
    }

    @Test
    void getUserReturnsProfileAndProgramId() {
        request().get("/api/users/" + seed.beginner().getId()).then().statusCode(200)
                .body("name", equalTo("Anna"), "email", equalTo("anna@test.dk"), "age", equalTo(25),
                        "height", equalTo(170f), "weight", equalTo(65f), "experienceLevel", equalTo("BEGINNER"),
                        "trainingDaysPerWeek", equalTo(3), "goal", equalTo("MUSCLE_GAIN"),
                        "workoutProgramId", equalTo(seed.fullBody().getId()));
    }

    @Test
    void createUserPersistsProfileAndCanBeRead() {
        UserRequestDTO user = new UserRequestDTO("Mia", "mia@test.dk", 24, 165, 60,
                ExperienceLevel.INTERMEDIATE, 2, TrainingGoal.GENERAL_FITNESS);
        int id = request().body(user).post("/api/users").then().statusCode(201)
                .body("id", greaterThan(0), "name", equalTo("Mia"), "workoutProgramId", nullValue())
                .extract().path("id");
        request().get("/api/users/" + id).then().statusCode(200)
                .body("email", equalTo("mia@test.dk"), "goal", equalTo("GENERAL_FITNESS"));
        assertEquals(2, new UserDAOImpl(emf).getById(id).getTrainingDaysPerWeek());
    }

    @Test
    void coachReturnsControlledAnswerAndReceivesPrompt() {
        request().header("Authorization", bearerToken()).body(Map.of("prompt", "How should I start training?"))
                .post("/api/coach")
                .then().statusCode(200).contentType(ContentType.JSON).body("answer", equalTo(ai.answer));
        assertEquals("How should I start training?", ai.lastPrompt);
    }

    @Test
    void registerPersistsHashedPasswordWithoutReturningIt() {
        RegisterRequestDTO registration = registration("register@test.dk", "plain-password");

        request().body(registration).post("/auth/register").then().statusCode(201)
                .body("id", greaterThan(0), "email", equalTo("register@test.dk"), "password", nullValue());

        User stored = new UserDAOImpl(emf).getByEmail("register@test.dk");
        assertNotNull(stored);
        assertNotEquals("plain-password", stored.getPassword());
        assertTrue(new PasswordService().verifyPassword("plain-password", stored.getPassword()));
    }

    @Test
    void loginReturnsValidToken() {
        request().body(registration("login@test.dk", "correct-password")).post("/auth/register")
                .then().statusCode(201);

        String token = request().body(new LoginRequestDTO("login@test.dk", "correct-password"))
                .post("/auth/login").then().statusCode(200).body("token", not(blankOrNullString()))
                .extract().path("token");

        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void invalidLoginReturns401() {
        request().body(new LoginRequestDTO("unknown@test.dk", "wrong-password")).post("/auth/login")
                .then().statusCode(401).body("status", equalTo(401), "msg", equalTo("Invalid email or password"));
    }

    @Test
    void protectedCoachRejectsMissingAuthorizationHeader() {
        request().body(Map.of("prompt", "How should I start training?")).post("/api/coach")
                .then().statusCode(401).body("status", equalTo(401), "msg", equalTo("Unauthorized"));
        assertNull(ai.lastPrompt);
    }

    @Test
    void protectedCoachRejectsMalformedAuthorizationHeader() {
        request().header("Authorization", "Token abc").body(Map.of("prompt", "How should I start training?"))
                .post("/api/coach").then().statusCode(401)
                .body("status", equalTo(401), "msg", equalTo("Unauthorized"));
        assertNull(ai.lastPrompt);
    }

    @Test
    void protectedCoachRejectsInvalidToken() {
        request().header("Authorization", "Bearer invalid-token")
                .body(Map.of("prompt", "How should I start training?")).post("/api/coach")
                .then().statusCode(401).body("status", equalTo(401), "msg", equalTo("Unauthorized"));
        assertNull(ai.lastPrompt);
    }

    @Test
    void generateProgramPersistsExercisesAndAssignsUser() {
        int userId = seed.beginner().getId();
        int programId = request().post("/api/users/" + userId + "/workout-program").then().statusCode(201)
                .body("id", greaterThan(0), "name", equalTo("AI Full Body"), "description", equalTo("A simple program"),
                        "trainingDaysPerWeek", equalTo(3), "exercises", hasSize(2),
                        "exercises.name", contains("Push Up", "Lunge"), "exercises.id", everyItem(greaterThan(0)))
                .extract().path("id");
        User user = new UserDAOImpl(emf).getById(userId);
        WorkoutProgram program = new WorkoutProgramDAOImpl(emf).getById(programId);
        assertEquals(programId, user.getWorkoutProgram().getId());
        assertEquals(2, program.getExercises().size());
        assertEquals(4, new ExerciseDAOImpl(emf).getAll().size());
        assertEquals(3, new WorkoutProgramDAOImpl(emf).getAll().size());
        assertEquals(userId, ai.lastUser.getId());
        assertEquals(TrainingGoal.MUSCLE_GAIN, ai.lastUser.getGoal());
        request().get("/api/users/" + userId).then().statusCode(200).body("workoutProgramId", equalTo(programId));
    }

    @Test
    void invalidExerciseReturns400WithoutSaving() {
        ExerciseRequestDTO exercise = new ExerciseRequestDTO("Curl", MuscleGroup.BICEPS, 0, 12);
        request().body(exercise).post("/api/exercises").then().statusCode(400)
                .body("status", equalTo(400), "msg", containsString("Sets"));
        assertEquals(2, new ExerciseDAOImpl(emf).getAll().size());
    }

    @Test
    void tooManyTrainingDaysReturns400WithoutSavingUser() {
        UserRequestDTO user = new UserRequestDTO("Mia", "mia@test.dk", 24, 165, 60,
                ExperienceLevel.BEGINNER, 8, TrainingGoal.MUSCLE_GAIN);
        request().body(user).post("/api/users").then().statusCode(400)
                .body("status", equalTo(400), "msg", containsString("Training days"));
        assertEquals(2, new UserDAOImpl(emf).getAll().size());
    }

    @Test
    void emptyCoachPromptReturns400() {
        request().header("Authorization", bearerToken()).body(Map.of("prompt", "")).post("/api/coach")
                .then().statusCode(400)
                .body("status", equalTo(400), "msg", equalTo("Prompt cannot be empty"));
        assertNull(ai.lastPrompt);
    }

    @Test
    void missingExerciseReturns404() {
        request().get("/api/exercises/2147483647").then().statusCode(404).contentType(ContentType.JSON)
                .body("status", equalTo(404), "msg", equalTo("Exercise not found"));
    }

    @Test
    void generatedProgramWithoutExercisesReturns502() {
        ai.program = new GeneratedWorkoutProgramDTO("AI Program", "A simple program", List.of());
        request().post("/api/users/" + seed.beginner().getId() + "/workout-program").then().statusCode(502)
                .body("status", equalTo(502), "msg", equalTo("AI generated an invalid workout program"));
        assertOriginalData();
    }

    @Test
    void failedProgramSaveRollsBackChanges() {
        // PostgreSQL rejects this description because the column allows at most 255 characters.
        ai.program = new GeneratedWorkoutProgramDTO("AI Program", "x".repeat(300), validProgram().exercises());
        request().post("/api/users/" + seed.beginner().getId() + "/workout-program").then().statusCode(500)
                .body("status", equalTo(500), "msg", equalTo("Internal server error"));
        assertOriginalData();
    }

    private void assertOriginalData() {
        assertEquals(2, new ExerciseDAOImpl(emf).getAll().size());
        assertEquals(2, new WorkoutProgramDAOImpl(emf).getAll().size());
        User user = new UserDAOImpl(emf).getById(seed.beginner().getId());
        assertEquals(seed.fullBody().getId(), user.getWorkoutProgram().getId());
    }

    private static GeneratedWorkoutProgramDTO validProgram() {
        return new GeneratedWorkoutProgramDTO("AI Full Body", "A simple program",
                List.of(new GeneratedExerciseDTO("Push Up", MuscleGroup.CHEST, 3, 12),
                        new GeneratedExerciseDTO("Lunge", MuscleGroup.LEGS, 3, 10)));
    }

    private RegisterRequestDTO registration(String email, String password) {
        return new RegisterRequestDTO("Test User", email, password, 25, 180, 80, ExperienceLevel.BEGINNER, 3,
                TrainingGoal.MUSCLE_GAIN);
    }

    private String bearerToken() {
        return "Bearer " + jwtService.generateToken(seed.beginner());
    }
}
