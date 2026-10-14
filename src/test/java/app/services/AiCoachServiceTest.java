package app.services;

import app.dto.GeneratedWorkoutProgramDTO;
import app.entities.ExperienceLevel;
import app.entities.MuscleGroup;
import app.entities.TrainingGoal;
import app.entities.User;
import app.exceptions.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AiCoachServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private Javalin fakeGemini;
    private AiCoachService service;
    private String responseBody;
    private int responseStatus;
    private String requestBody;

    @BeforeEach
    void setUp() throws IOException {
        responseStatus = 200;
        responseBody = geminiResponse("Start with three sessions.");
        fakeGemini = Javalin.create();
        fakeGemini.post("/generate", ctx -> {
            requestBody = ctx.body();
            ctx.status(responseStatus).contentType("application/json").result(responseBody);
        });
        fakeGemini.start(0);
        URI apiUrl = URI.create("http://localhost:" + fakeGemini.port() + "/generate");
        service = new AiCoachService("test-key", apiUrl);
    }

    @AfterEach
    void tearDown() {
        if (fakeGemini != null) {
            fakeGemini.stop();
        }
    }

    @Test
    void askCoachReturnsAnswerFromGeminiJson() throws IOException, InterruptedException {
        String answer = service.askCoach("How should I start?");
        assertEquals("Start with three sessions.", answer);
        JsonNode request = mapper.readTree(requestBody);
        assertTrue(request.at("/contents/0/parts/0/text").asText().contains("How should I start?"));
    }

    @Test
    void generateWorkoutProgramParsesJsonAndUsesUserProfile() throws IOException, InterruptedException {
        String programJson = mapper.writeValueAsString(Map.of("name", "Full Body", "description", "Simple program",
                "exercises", List.of(Map.of("name", "Squat", "muscleGroup", "LEGS", "sets", 3, "reps", 10))));
        responseBody = geminiResponse(programJson);
        User user = new User("Anna", "anna@test.dk", 25, 170, 65, ExperienceLevel.BEGINNER, 3, TrainingGoal.MUSCLE_GAIN);
        GeneratedWorkoutProgramDTO program = service.generateWorkoutProgram(user);
        assertEquals("Full Body", program.name());
        assertEquals(1, program.exercises().size());
        assertEquals(MuscleGroup.LEGS, program.exercises().get(0).muscleGroup());
        JsonNode request = mapper.readTree(requestBody);
        assertTrue(request.at("/contents/0/parts/0/text").asText().contains("Training days per week: 3"));
    }

    @Test
    void failedGeminiRequestThrowsApiException() {
        responseStatus = 429;
        responseBody = "Too many requests";
        ApiException error = assertThrows(ApiException.class, () -> service.askCoach("Question"));
        assertEquals(502, error.getCode());
    }

    private String geminiResponse(String answer) throws IOException {
        return mapper.writeValueAsString(Map.of("candidates",
                List.of(Map.of("content", Map.of("parts", List.of(Map.of("text", answer)))))));
    }
}
