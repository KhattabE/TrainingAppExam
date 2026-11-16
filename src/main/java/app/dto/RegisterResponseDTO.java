package app.dto;

import app.entities.ExperienceLevel;
import app.entities.TrainingGoal;

public record RegisterResponseDTO(int id, String name, String email, int age, double height, double weight, ExperienceLevel experienceLevel, int trainingDaysPerWeek, TrainingGoal goal, Integer workoutProgramId, String token) {
    public RegisterResponseDTO(UserResponseDTO user, String token) {
        this(user.id(), user.name(), user.email(), user.age(), user.height(), user.weight(), user.experienceLevel(), user.trainingDaysPerWeek(), user.goal(), user.workoutProgramId(), token);
    }
}
