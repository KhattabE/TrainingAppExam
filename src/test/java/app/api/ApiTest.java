
package app.api;

import app.dao.ExerciseDAOImpl;
import app.dao.UserDAO;
import app.dao.UserDAOImpl;
import app.dao.WorkoutProgramDAOImpl;
import app.dto.GeneratedExerciseDTO;
import app.dto.GeneratedWorkoutProgramDTO;
import app.dto.LoginRequestDTO;
import app.dto.RegisterRequestDTO;
import app.dto.UpdateUserRequestDTO;
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

import java.util.HashMap;
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
        return given()
                .baseUri("http://localhost")
                .port(app.port())
                .contentType(ContentType.JSON);
    }

    @Test
    void healthReturnsJson() {
        request().get("/api/health")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("ok"),
                        "message", equalTo("API is running"));
    }

    @Test
    void listExercisesReturnsSeededDtos() {
        request().get("/api/exercises")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", containsInAnyOrder(
                                seed.benchPress().getId(),
                                seed.squat().getId()),
                        "name", containsInAnyOrder("Bench Press", "Squat"));
    }

    @Test
    void getExerciseReturnsItsFields() {
        request().get("/api/exercises/" + seed.benchPress().getId())
                .then().statusCode(200)
                .body("id", equalTo(seed.benchPress().getId()),
                        "name", equalTo("Bench Press"),
                        "muscleGroup", equalTo("CHEST"),
                        "sets", equalTo(3),
                        "reps", equalTo(10));
    }

    @Test
    void createExerciseRouteIsNotAvailable() {
        request()
                .post("/api/exercises")
                .then().statusCode(405);
    }

    @Test
    void updateExerciseRouteIsNotAvailable() {
        request()
                .put("/api/exercises/" + seed.benchPress().getId())
                .then().statusCode(405);
    }

    @Test
    void deleteExerciseRouteIsNotAvailable() {
        request()
                .delete("/api/exercises/" + seed.benchPress().getId())
                .then().statusCode(405);
    }

    @Test
    void currentUserRejectsMissingToken() {
        request()
                .get("/api/users/me")
                .then().statusCode(401)
                .body("status", equalTo(401),
                        "msg", equalTo("Unauthorized"));
    }

    @Test
    void currentUserRejectsInvalidToken() {
        request()
                .header("Authorization", "Bearer invalid-token")
                .get("/api/users/me")
                .then().statusCode(401)
                .body("status", equalTo(401),
                        "msg", equalTo("Unauthorized"));
    }

    @Test
    void currentUserReturnsOwnProfileWithoutPassword() {
        request()
                .header("Authorization", bearerToken())
                .get("/api/users/me")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(seed.beginner().getId()),
                        "name", equalTo("Anna"),
                        "email", equalTo("anna@test.dk"),
                        "age", equalTo(25),
                        "height", equalTo(170f),
                        "weight", equalTo(65f),
                        "experienceLevel", equalTo("BEGINNER"),
                        "trainingDaysPerWeek", equalTo(3),
                        "goal", equalTo("MUSCLE_GAIN"),
                        "workoutProgramId", equalTo(seed.fullBody().getId()),
                        "$", not(hasKey("password")));
    }

    @Test
    void updateCurrentUserRejectsMissingToken() {
        request()
                .body(new UpdateUserRequestDTO("Anna Updated", 26, 172, 68, ExperienceLevel.INTERMEDIATE, 4, TrainingGoal.STRENGTH))
                .put("/api/users/me")
                .then().statusCode(401)
                .body("status", equalTo(401),
                        "msg", equalTo("Unauthorized"));
    }

    @Test
    void updateCurrentUserRejectsInvalidToken() {
        request()
                .header("Authorization", "Bearer invalid-token")
                .body(new UpdateUserRequestDTO("Anna Updated", 26, 172, 68, ExperienceLevel.INTERMEDIATE, 4, TrainingGoal.STRENGTH))
                .put("/api/users/me")
                .then().statusCode(401)
                .body("status", equalTo(401),
                        "msg", equalTo("Unauthorized"));
    }

    @Test
    void updateCurrentUserPersistsProfileAndPreservesProtectedFields() {
        UserDAO userDAO = new UserDAOImpl(emf);
        User originalUser = userDAO.getById(seed.beginner().getId());
        String originalPassword = originalUser.getPassword();
        int originalWorkoutProgramId = originalUser.getWorkoutProgram().getId();
        UpdateUserRequestDTO update = new UpdateUserRequestDTO("  Anna Updated  ", 26, 172, 68, ExperienceLevel.INTERMEDIATE, 4, TrainingGoal.STRENGTH);

        request()
                .header("Authorization", bearerToken())
                .body(update)
                .put("/api/users/me")
                .then().statusCode(200)
                .body("id", equalTo(seed.beginner().getId()),
                        "name", equalTo("Anna Updated"),
                        "email", equalTo("anna@test.dk"),
                        "age", equalTo(26),
                        "height", equalTo(172f),
                        "weight", equalTo(68f),
                        "experienceLevel", equalTo("INTERMEDIATE"),
                        "trainingDaysPerWeek", equalTo(4),
                        "goal", equalTo("STRENGTH"),
                        "workoutProgramId", equalTo(originalWorkoutProgramId),
                        "$", not(hasKey("password")));

        User storedUser = userDAO.getById(seed.beginner().getId());
        assertEquals("Anna Updated", storedUser.getName());
        assertEquals(26, storedUser.getAge());
        assertEquals(172, storedUser.getHeight());
        assertEquals(68, storedUser.getWeight());
        assertEquals(ExperienceLevel.INTERMEDIATE, storedUser.getExperienceLevel());
        assertEquals(4, storedUser.getTrainingDaysPerWeek());
        assertEquals(TrainingGoal.STRENGTH, storedUser.getGoal());
        assertEquals("anna@test.dk", storedUser.getEmail());
        assertEquals(originalPassword, storedUser.getPassword());
        assertEquals(originalWorkoutProgramId, storedUser.getWorkoutProgram().getId());
    }

    @Test
    void updateCurrentUserRejectsInvalidProfileWithoutPersistingChanges() {
        UpdateUserRequestDTO update = new UpdateUserRequestDTO(" ", 0, 0, 0, null, 0, null);

        request()
                .header("Authorization", bearerToken())
                .body(update)
                .put("/api/users/me")
                .then().statusCode(400)
                .body("status", equalTo(400),
                        "msg", allOf(containsString("Name is required"), containsString("Age must be greater than 0"), containsString("Height must be a finite number greater than 0"), containsString("Weight must be a finite number greater than 0"), containsString("Experience level is required"), containsString("Training days must be between 1 and 7"), containsString("Training goal is required")));

        User storedUser = new UserDAOImpl(emf).getById(seed.beginner().getId());
        assertEquals("Anna", storedUser.getName());
        assertEquals(25, storedUser.getAge());
        assertEquals(3, storedUser.getTrainingDaysPerWeek());
        assertEquals(TrainingGoal.MUSCLE_GAIN, storedUser.getGoal());
    }

    @Test
    void updateCurrentUserRejectsValuesThatCannotBeStored() {
        UpdateUserRequestDTO update = new UpdateUserRequestDTO("x".repeat(256), 25, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, ExperienceLevel.BEGINNER, 3, TrainingGoal.MUSCLE_GAIN);

        request()
                .header("Authorization", bearerToken())
                .body(update)
                .put("/api/users/me")
                .then().statusCode(400)
                .body("status", equalTo(400),
                        "msg", allOf(containsString("Name must be at most 255 characters"), containsString("Height must be a finite number greater than 0"), containsString("Weight must be a finite number greater than 0")));

        User storedUser = new UserDAOImpl(emf).getById(seed.beginner().getId());
        assertEquals("Anna", storedUser.getName());
        assertEquals(170, storedUser.getHeight());
        assertEquals(65, storedUser.getWeight());
    }

    @Test
    void updateCurrentUserRejectsProtectedFields() {
        UserDAO userDAO = new UserDAOImpl(emf);
        User originalUser = userDAO.getById(seed.beginner().getId());
        String originalPassword = originalUser.getPassword();
        Map<String, Object> update = new HashMap<>();
        update.put("name", "Anna Updated");
        update.put("age", 26);
        update.put("height", 172);
        update.put("weight", 68);
        update.put("experienceLevel", "INTERMEDIATE");
        update.put("trainingDaysPerWeek", 4);
        update.put("goal", "STRENGTH");
        update.put("email", "attacker@test.dk");
        update.put("password", "new-password");

        request()
                .header("Authorization", bearerToken())
                .body(update)
                .put("/api/users/me")
                .then().statusCode(400);

        User storedUser = userDAO.getById(seed.beginner().getId());
        assertEquals("anna@test.dk", storedUser.getEmail());
        assertEquals(originalPassword, storedUser.getPassword());
        assertEquals("Anna", storedUser.getName());
    }

    @Test
    void updateCurrentUserRejectsFractionalWholeNumberFields() {
        Map<String, Object> update = new HashMap<>();
        update.put("name", "Anna Updated");
        update.put("age", 25.5);
        update.put("height", 172);
        update.put("weight", 68);
        update.put("experienceLevel", "INTERMEDIATE");
        update.put("trainingDaysPerWeek", 3.5);
        update.put("goal", "STRENGTH");

        request()
                .header("Authorization", bearerToken())
                .body(update)
                .put("/api/users/me")
                .then().statusCode(400);

        User storedUser = new UserDAOImpl(emf).getById(seed.beginner().getId());
        assertEquals(25, storedUser.getAge());
        assertEquals(3, storedUser.getTrainingDaysPerWeek());
    }

    @Test
    void updateCurrentUserOnlyChangesAuthenticatedUser() {
        String advancedUserToken = "Bearer " + jwtService.generateToken(seed.advanced());
        UpdateUserRequestDTO update = new UpdateUserRequestDTO("Omar Updated", 31, 186, 86, ExperienceLevel.INTERMEDIATE, 5, TrainingGoal.GENERAL_FITNESS);

        request()
                .header("Authorization", advancedUserToken)
                .body(update)
                .put("/api/users/me")
                .then().statusCode(200)
                .body("id", equalTo(seed.advanced().getId()),
                        "name", equalTo("Omar Updated"),
                        "email", equalTo("omar@test.dk"));

        UserDAO userDAO = new UserDAOImpl(emf);
        assertEquals("Omar Updated", userDAO.getById(seed.advanced().getId()).getName());
        assertEquals("Anna", userDAO.getById(seed.beginner().getId()).getName());
    }

    @Test
    void oldUserRoutesAreNotAvailable() {
        request()
                .get("/api/users")
                .then().statusCode(404);

        request()
                .get("/api/users/" + seed.beginner().getId())
                .then().statusCode(404);

        request()
                .post("/api/users")
                .then().statusCode(404);
    }

    @Test
    void coachReturnsControlledAnswerAndReceivesPrompt() {
        request()
                .header("Authorization", bearerToken())
                .body(Map.of("prompt", "How should I start training?"))
                .post("/api/coach")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("answer", equalTo(ai.answer));

        assertEquals("How should I start training?", ai.lastPrompt);
    }

    @Test
    void registerPersistsHashedPasswordWithoutReturningIt() {
        RegisterRequestDTO registration =
                registration("  REGISTER@Test.DK  ", "plain-password");

        request().body(registration)
                .post("/auth/register")
                .then().statusCode(201)
                .body("id", greaterThan(0),
                        "email", equalTo("register@test.dk"),
                        "password", nullValue());

        User stored = new UserDAOImpl(emf)
                .getByEmail("register@test.dk");

        assertNotNull(stored);
        assertNotEquals("plain-password", stored.getPassword());
        assertTrue(new PasswordService().verifyPassword(
                "plain-password", stored.getPassword()));
    }

    @Test
    void overlongRegistrationFieldsReturn400WithoutRegisteringUser() {
        RegisterRequestDTO registration = new RegisterRequestDTO("n".repeat(256), "e".repeat(244) + "@example.com", "password", 24, 165, 60, ExperienceLevel.BEGINNER, 3, TrainingGoal.MUSCLE_GAIN);

        request().body(registration)
                .post("/auth/register")
                .then().statusCode(400)
                .body("status", equalTo(400),
                        "msg", allOf(containsString("Name must be at most 255 characters"), containsString("Email must be at most 255 characters")));

        assertEquals(2, new UserDAOImpl(emf).getAll().size());
    }

    @Test
    void loginReturnsValidToken() {
        request()
                .body(registration("login@test.dk", "correct-password"))
                .post("/auth/register")
                .then().statusCode(201);

        String token = request()
                .body(new LoginRequestDTO("login@test.dk", "correct-password"))
                .post("/auth/login")
                .then().statusCode(200)
                .body("token", not(blankOrNullString()))
                .extract().path("token");

        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void invalidLoginReturns401() {
        request()
                .body(new LoginRequestDTO("unknown@test.dk", "wrong-password"))
                .post("/auth/login")
                .then().statusCode(401)
                .body("status", equalTo(401),
                        "msg", equalTo("Invalid email or password"));
    }

    @Test
    void protectedCoachRejectsMissingAuthorizationHeader() {
        request()
                .body(Map.of("prompt", "How should I start training?"))
                .post("/api/coach")
                .then().statusCode(401)
                .body("status", equalTo(401),
                        "msg", equalTo("Unauthorized"));

        assertNull(ai.lastPrompt);
    }

    @Test
    void protectedCoachRejectsMalformedAuthorizationHeader() {
        request()
                .header("Authorization", "Token abc")
                .body(Map.of("prompt", "How should I start training?"))
                .post("/api/coach")
                .then().statusCode(401)
                .body("status", equalTo(401),
                        "msg", equalTo("Unauthorized"));

        assertNull(ai.lastPrompt);
    }

    @Test
    void protectedCoachRejectsInvalidToken() {
        request()
                .header("Authorization", "Bearer invalid-token")
                .body(Map.of("prompt", "How should I start training?"))
                .post("/api/coach")
                .then().statusCode(401)
                .body("status", equalTo(401),
                        "msg", equalTo("Unauthorized"));

        assertNull(ai.lastPrompt);
    }

    @Test
    void generateProgramPersistsExercisesAndAssignsUser() {
        int userId = seed.beginner().getId();

        int programId = request()
                .header("Authorization", bearerToken())
                .post("/api/users/" + userId + "/workout-program")
                .then().statusCode(201)
                .body("id", greaterThan(0),
                        "name", equalTo("AI Full Body"),
                        "description", equalTo("A simple program"),
                        "trainingDaysPerWeek", equalTo(3),
                        "exercises", hasSize(2),
                        "exercises.name", contains("Push Up", "Lunge"),
                        "exercises.id", everyItem(greaterThan(0)))
                .extract().path("id");

        User user = new UserDAOImpl(emf).getById(userId);
        WorkoutProgram program = new WorkoutProgramDAOImpl(emf).getById(programId);

        assertEquals(programId, user.getWorkoutProgram().getId());
        assertEquals(2, program.getExercises().size());
        assertEquals(3, new ExerciseDAOImpl(emf).getAll().size());
        assertEquals(2, new WorkoutProgramDAOImpl(emf).getAll().size());
        assertNull(new WorkoutProgramDAOImpl(emf).getById(seed.fullBody().getId()));
        assertNull(new ExerciseDAOImpl(emf).getById(seed.squat().getId()));
        assertNotNull(new ExerciseDAOImpl(emf).getById(seed.benchPress().getId()));
        assertNotNull(new WorkoutProgramDAOImpl(emf).getById(seed.upperBody().getId()));
        assertEquals(userId, ai.lastUser.getId());
        assertEquals(TrainingGoal.MUSCLE_GAIN, ai.lastUser.getGoal());

        request()
                .header("Authorization", bearerToken())
                .get("/api/users/me")
                .then().statusCode(200)
                .body("workoutProgramId", equalTo(programId));
    }

    @Test
    void generatingAgainReplacesPreviousProgramAndExercises() {
        int userId = seed.beginner().getId();
        int firstProgramId = request().header("Authorization", bearerToken()).post("/api/users/" + userId + "/workout-program").then().statusCode(201).extract().path("id");
        List<Integer> firstExerciseIds = new WorkoutProgramDAOImpl(emf).getById(firstProgramId).getExercises().stream().map(exercise -> exercise.getId()).toList();
        ai.program = new GeneratedWorkoutProgramDTO("AI Strength", "A stronger program", List.of(new GeneratedExerciseDTO("Row", MuscleGroup.BACK, 4, 8), new GeneratedExerciseDTO("Plank", MuscleGroup.CORE, 3, 30)));

        int secondProgramId = request().header("Authorization", bearerToken()).post("/api/users/" + userId + "/workout-program").then().statusCode(201).extract().path("id");

        assertNotEquals(firstProgramId, secondProgramId);
        assertEquals(secondProgramId, new UserDAOImpl(emf).getById(userId).getWorkoutProgram().getId());
        assertNull(new WorkoutProgramDAOImpl(emf).getById(firstProgramId));
        assertTrue(firstExerciseIds.stream().allMatch(id -> new ExerciseDAOImpl(emf).getById(id) == null));
        assertEquals(2, new WorkoutProgramDAOImpl(emf).getAll().size());
        assertEquals(3, new ExerciseDAOImpl(emf).getAll().size());
    }

    @Test
    void generatingProgramPreservesOldProgramUsedByAnotherUser() {
        UserDAO userDAO = new UserDAOImpl(emf);
        User advancedUser = userDAO.getById(seed.advanced().getId());
        advancedUser.setWorkoutProgram(seed.fullBody());
        userDAO.update(advancedUser);

        request().header("Authorization", bearerToken()).post("/api/users/" + seed.beginner().getId() + "/workout-program").then().statusCode(201);

        assertEquals(seed.fullBody().getId(), userDAO.getById(seed.advanced().getId()).getWorkoutProgram().getId());
        assertNotNull(new WorkoutProgramDAOImpl(emf).getById(seed.fullBody().getId()));
        assertNotNull(new ExerciseDAOImpl(emf).getById(seed.benchPress().getId()));
        assertNotNull(new ExerciseDAOImpl(emf).getById(seed.squat().getId()));
        assertEquals(3, new WorkoutProgramDAOImpl(emf).getAll().size());
        assertEquals(4, new ExerciseDAOImpl(emf).getAll().size());
    }

    @Test
    void tooManyTrainingDaysReturns400WithoutRegisteringUser() {
        RegisterRequestDTO registration = new RegisterRequestDTO("Mia", "mia@test.dk", "password", 24, 165, 60, ExperienceLevel.BEGINNER, 8, TrainingGoal.MUSCLE_GAIN);

        request().body(registration)
                .post("/auth/register")
                .then().statusCode(400)
                .body("status", equalTo(400),
                        "msg", containsString("Training days"));

        assertEquals(2, new UserDAOImpl(emf).getAll().size());
    }

    @Test
    void emptyCoachPromptReturns400() {
        request()
                .header("Authorization", bearerToken())
                .body(Map.of("prompt", ""))
                .post("/api/coach")
                .then().statusCode(400)
                .body("status", equalTo(400),
                        "msg", equalTo("Prompt cannot be empty"));

        assertNull(ai.lastPrompt);
    }

    @Test
    void missingExerciseReturns404() {
        request().get("/api/exercises/2147483647")
                .then().statusCode(404)
                .contentType(ContentType.JSON)
                .body("status", equalTo(404),
                        "msg", equalTo("Exercise not found"));
    }

    @Test
    void generatedProgramWithoutExercisesReturns502() {
        ai.program = new GeneratedWorkoutProgramDTO(
                "AI Program", "A simple program", List.of()
        );

        request()
                .header("Authorization", bearerToken())
                .post("/api/users/" + seed.beginner().getId() + "/workout-program")
                .then().statusCode(502)
                .body("status", equalTo(502),
                        "msg", equalTo("AI generated an invalid workout program"));

        assertOriginalData();
    }

    @Test
    void generatedProgramWithTooLongDescriptionReturns502() {
        ai.program = new GeneratedWorkoutProgramDTO(
                "AI Program", "x".repeat(256),
                validProgram().exercises()
        );

        request()
                .header("Authorization", bearerToken())
                .post("/api/users/" + seed.beginner().getId() + "/workout-program")
                .then().statusCode(502)
                .body("status", equalTo(502),
                        "msg", equalTo("AI generated an invalid workout program"));

        assertOriginalData();
    }

    @Test
    void generatedProgramWithTooLongNameReturns502() {
        ai.program = new GeneratedWorkoutProgramDTO(
                "x".repeat(256), "A simple program",
                validProgram().exercises()
        );

        request()
                .header("Authorization", bearerToken())
                .post("/api/users/" + seed.beginner().getId() + "/workout-program")
                .then().statusCode(502)
                .body("status", equalTo(502),
                        "msg", equalTo("AI generated an invalid workout program"));

        assertOriginalData();
    }

    @Test
    void generatedExerciseWithTooLongNameReturns502() {
        ai.program = new GeneratedWorkoutProgramDTO(
                "AI Program", "A simple program",
                List.of(new GeneratedExerciseDTO("x".repeat(256), MuscleGroup.CHEST, 3, 10))
        );

        request()
                .header("Authorization", bearerToken())
                .post("/api/users/" + seed.beginner().getId() + "/workout-program")
                .then().statusCode(502)
                .body("status", equalTo(502),
                        "msg", equalTo("AI generated an invalid exercise"));

        assertOriginalData();
    }

    @Test
    void generatedTextAtMaximumLengthIsAccepted() {
        String programName = "p".repeat(255);
        String description = "d".repeat(255);
        String exerciseName = "e".repeat(255);
        ai.program = new GeneratedWorkoutProgramDTO(programName, description, List.of(new GeneratedExerciseDTO(exerciseName, MuscleGroup.CHEST, 3, 10)));

        request()
                .header("Authorization", bearerToken())
                .post("/api/users/" + seed.beginner().getId() + "/workout-program")
                .then().statusCode(201)
                .body("name", equalTo(programName),
                        "description", equalTo(description),
                        "exercises[0].name", equalTo(exerciseName));
    }

    @Test
    void workoutProgramRejectsMissingToken() {
        request()
                .get("/api/users/" + seed.beginner().getId() + "/workout-program")
                .then().statusCode(401)
                .body("status", equalTo(401),
                        "msg", equalTo("Unauthorized"));
    }

    @Test
    void workoutProgramRejectsInvalidToken() {
        request()
                .header("Authorization", "Bearer invalid-token")
                .get("/api/users/" + seed.beginner().getId() + "/workout-program")
                .then().statusCode(401)
                .body("status", equalTo(401),
                        "msg", equalTo("Unauthorized"));
    }

    @Test
    void workoutProgramRejectsOtherUsers() {
        String advancedUserToken =
                "Bearer " + jwtService.generateToken(seed.advanced());

        request()
                .header("Authorization", advancedUserToken)
                .get("/api/users/" + seed.beginner().getId() + "/workout-program")
                .then().statusCode(403)
                .body("status", equalTo(403),
                        "msg", equalTo("You cannot access another user's workout program"));
    }

    @Test
    void workoutProgramAllowsOwnerToReadProgram() {
        request()
                .header("Authorization", bearerToken())
                .get("/api/users/" + seed.beginner().getId() + "/workout-program")
                .then().statusCode(200)
                .body("id", equalTo(seed.fullBody().getId()));
    }

    private void assertOriginalData() {
        assertEquals(2, new ExerciseDAOImpl(emf).getAll().size());
        assertEquals(2, new WorkoutProgramDAOImpl(emf).getAll().size());

        User user = new UserDAOImpl(emf)
                .getById(seed.beginner().getId());

        assertEquals(seed.fullBody().getId(), user.getWorkoutProgram().getId());
    }

    private static GeneratedWorkoutProgramDTO validProgram() {
        return new GeneratedWorkoutProgramDTO(
                "AI Full Body",
                "A simple program",
                List.of(new GeneratedExerciseDTO("Push Up", MuscleGroup.CHEST, 3, 12), new GeneratedExerciseDTO("Lunge", MuscleGroup.LEGS, 3, 10)));
    }

    private RegisterRequestDTO registration(String email, String password) {
        return new RegisterRequestDTO("Test User", email, password, 25, 180, 80, ExperienceLevel.BEGINNER, 3, TrainingGoal.MUSCLE_GAIN);
    }

    private String bearerToken() {
        return "Bearer " + jwtService.generateToken(seed.beginner());
    }
}
