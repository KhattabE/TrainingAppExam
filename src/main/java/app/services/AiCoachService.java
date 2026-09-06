package app.services;

import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import tools.jackson.databind.JsonNode;
import app.dto.GeminiContentDTO;
import app.dto.GeminiPartDTO;
import app.dto.GeminiRequestDTO;

public class AiCoachService {
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.7-flash:generateContent";

    public String askCoach(String question) throws IOException, InterruptedException {
        GeminiPartDTO part = new GeminiPartDTO(question);

        GeminiContentDTO content = new GeminiContentDTO(
                List.of(part)
        );

        GeminiRequestDTO requestBody = new GeminiRequestDTO(
                List.of(content)
        );

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Gemini API error: " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());

        String answer = root
                .get("candidates")
                .get(0)
                .get("content")
                .get("parts")
                .get(0)
                .get("text")
                .asText();

        return answer;
    }
}
