package app.api;

import app.dto.CoachRequestDTO;
import app.dto.CoachResponseDTO;
import app.services.AiCoachService;

import io.javalin.http.Context;

import java.io.IOException;

public class CoachController {

    private final AiCoachService aiCoachService;

    public CoachController(AiCoachService aiCoachService) {
        this.aiCoachService = aiCoachService;
    }


    public void ask(Context ctx)
            throws IOException, InterruptedException {

        CoachRequestDTO request = ctx
                .bodyValidator(CoachRequestDTO.class)
                .check(
                        requestBody ->
                                requestBody.prompt() != null
                                        && !requestBody.prompt()
                                        .isBlank(),
                        "Prompt cannot be empty"
                )
                .get();

        String answer = aiCoachService.askCoach(request.prompt());

        ctx.status(200);

        ctx.json(new CoachResponseDTO(answer));
    }
}