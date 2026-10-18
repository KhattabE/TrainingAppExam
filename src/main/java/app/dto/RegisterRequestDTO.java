package app.dto;

import app.entities.ExperienceLevel;
import app.entities.TrainingGoal;

public record RegisterRequestDTO(
        String name,
        String email,
        String password,
        int age,
        double height,
        double weight,
        ExperienceLevel experienceLevel,
        int trainingDaysPerWeek,
        TrainingGoal goal
) {
}
