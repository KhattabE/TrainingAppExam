package app.services;

import app.dto.GeneratedWorkoutProgramDTO;
import app.dto.GeminiResponseDTO;
import app.entities.User;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.List;
import java.util.Map;

public class AiCoachService {

    private static final Logger logger = LoggerFactory.getLogger(AiCoachService.class);

    private final String apiKey = System.getenv("GEMINI_API_KEY");

    private static final String MODEL = "gemini-3.5-flash-lite";

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL
                    + ":generateContent";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(
                            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            false
                    );

    public String askCoach(String question)
            throws IOException, InterruptedException {

        String prompt = """
                You are a concise AI fitness coach.

                Answer using simple, friendly language.
                Keep the answer between 2 and 4 short sentences.
                Never exceed 80 words.
                Do not include a title or introduction.

                If the question requires medical diagnosis,
                tell the user to consult a qualified
                healthcare professional.

                User question:
                %s
                """.formatted(question);

        return callGemini(
                prompt,
                250,
                false
        );
    }

    public GeneratedWorkoutProgramDTO generateWorkoutProgram(
            User user
    ) throws IOException, InterruptedException {

        String prompt = """
                You are an AI fitness coach creating a structured workout program.

                Create a safe and sensible workout program for this user.

                User profile:
                Age: %d
                Height: %.1f
                Weight: %.1f
                Experience level: %s
                Training goal: %s
                Training days per week: %d

                Return ONLY valid JSON.

                The JSON must have exactly this structure:

                {
                  "name": "Program name",
                  "description": "Short program description",
                  "exercises": [
                    {
                      "name": "Exercise name",
                      "muscleGroup": "CHEST",
                      "sets": 3,
                      "reps": 10
                    }
                  ]
                }

                Allowed muscleGroup values are ONLY:

                CHEST
                BACK
                SHOULDERS
                BICEPS
                TRICEPS
                LEGS
                CORE

                Use exercises appropriate for the user's experience level
                and training goal.

                The exercise list should contain enough exercises for a
                %d-day-per-week workout program.

                Do not include markdown.
                Do not include ```json.
                Do not include explanations outside the JSON.
                """.formatted(
                user.getAge(),
                user.getHeight(),
                user.getWeight(),
                user.getExperienceLevel(),
                user.getGoal(),
                user.getTrainingDaysPerWeek(),
                user.getTrainingDaysPerWeek()
        );

        String json = callGemini(
                prompt,
                1800,
                true
        );

        json = stripCodeFences(json);

        return objectMapper.readValue(
                json,
                GeneratedWorkoutProgramDTO.class
        );
    }

    private String callGemini(
            String prompt,
            int maxOutputTokens,
            boolean jsonMode
    ) throws IOException, InterruptedException {

        if (apiKey == null || apiKey.isBlank()) {

            throw new IllegalStateException(
                    "GEMINI_API_KEY is not configured"
            );
        }

        Map<String, Object> generationConfig;

        if (jsonMode) {

            generationConfig = Map.of(
                    "maxOutputTokens", maxOutputTokens,
                    "responseMimeType", "application/json"
            );

        } else {

            generationConfig = Map.of(
                    "maxOutputTokens", maxOutputTokens
            );
        }

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of(
                                                "text",
                                                prompt
                                        )
                                )
                        )
                ),
                "generationConfig",
                generationConfig
        );

        String jsonBody = objectMapper.writeValueAsString(body);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .header(
                                "x-goog-api-key",
                                apiKey
                        )
                        .POST(
                                HttpRequest.BodyPublishers
                                        .ofString(jsonBody)
                        )
                        .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        logger.info(
                "Gemini responded with status {}",
                response.statusCode()
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {

            throw new IllegalStateException("Gemini API returned status " + response.statusCode());

        }

        GeminiResponseDTO geminiResponse =
                objectMapper.readValue(
                        response.body(),
                        GeminiResponseDTO.class
                );

        if (geminiResponse.candidates() == null
                || geminiResponse.candidates().isEmpty()
                || geminiResponse.candidates().get(0).content() == null
                || geminiResponse.candidates().get(0).content().parts() == null
                || geminiResponse.candidates().get(0).content().parts().isEmpty()) {

            throw new IllegalStateException(
                    "Gemini returned no usable response"
            );
        }

        return geminiResponse
                .candidates()
                .get(0)
                .content()
                .parts()
                .get(0)
                .text();
    }


    private String stripCodeFences(String text) {

        return text
                .trim()
                .replaceFirst(
                        "^```(?:json)?\\s*",
                        ""
                )
                .replaceFirst(
                        "\\s*```$",
                        ""
                )
                .trim();
    }
}