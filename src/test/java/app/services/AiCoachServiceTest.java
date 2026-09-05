package app.services;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AiCoachServiceTest {

    @Test
    void askCoachShouldReturnResponse() throws IOException, InterruptedException {
        AiCoachService aiCoachService = new AiCoachService();

        String response = aiCoachService.askCoach("What are calories?");

        assertNotNull(response);
        assertFalse(response.isBlank());

        System.out.println(response);
    }
}
