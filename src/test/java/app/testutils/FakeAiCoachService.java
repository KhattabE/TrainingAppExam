package app.testutils;

import app.dto.GeneratedExerciseDTO;
import app.dto.GeneratedWorkoutProgramDTO;
import app.entities.MuscleGroup;
import app.entities.User;
import app.services.AiCoachService;

import java.net.URI;
import java.util.List;

public class FakeAiCoachService extends AiCoachService {

    public String answer = "Start with three full body sessions per week.";
    public GeneratedWorkoutProgramDTO program = new GeneratedWorkoutProgramDTO("AI Full Body", "A simple program",
            List.of(new GeneratedExerciseDTO("Push Up", MuscleGroup.CHEST, 3, 12),
                    new GeneratedExerciseDTO("Lunge", MuscleGroup.LEGS, 3, 10)));
    public String lastPrompt;
    public User lastUser;

    public FakeAiCoachService() {
        super("test-key", URI.create("http://127.0.0.1"));
    }

    @Override
    public String askCoach(String question) {
        lastPrompt = question;
        return answer;
    }

    @Override
    public GeneratedWorkoutProgramDTO generateWorkoutProgram(User user) {
        lastUser = user;
        return program;
    }
}
