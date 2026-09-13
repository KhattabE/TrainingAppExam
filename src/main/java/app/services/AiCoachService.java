package app.services;

import app.dto.GeminiResponseDTO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class AiCoachService {

    private final String apiKey = System.getenv("GEMINI_API_KEY");

    private static final String MODEL = "gemini-3.5-flash-lite";

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL
                    + ":generateContent";

    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(
                            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            false
                    );

    public String askCoach(String question)
            throws IOException, InterruptedException {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY is not configured"
            );
        }

        String prompt = """
        You are a concise AI fitness coach.

        Answer using simple, friendly language.
        Keep the answer between 2 and 4 short sentences.
        Never exceed 80 words.
        Do not include a title or introduction.
        If the question requires medical diagnosis, tell the user
        to consult a qualified healthcare professional.

        User question:
        %s
        """.formatted(question);

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
                "generationConfig", Map.of(
                        "maxOutputTokens",
                        250
                )
        );

        String jsonBody =
                objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        System.out.println("HTTP status: " + response.statusCode());
        System.out.println(response.body());

        GeminiResponseDTO geminiResponse =
                objectMapper.readValue(
                        response.body(),
                        GeminiResponseDTO.class
                );

        String answer = geminiResponse
                .candidates()
                .get(0)
                .content()
                .parts()
                .get(0)
                .text();

        System.out.println(answer);

        return answer;
    }
}
