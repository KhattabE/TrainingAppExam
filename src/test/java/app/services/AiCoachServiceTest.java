package app.services;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AiCoachServiceTest {

    @Test
    void askCoachShouldReturnGeneratedAnswer()
            throws IOException, InterruptedException {

        AiCoachService aiCoachService = new AiCoachService();

        String answer =
                aiCoachService.askCoach("What are calories?");

        assertNotNull(answer);
        assertFalse(answer.isBlank());

        System.out.println(answer);
    }
}
