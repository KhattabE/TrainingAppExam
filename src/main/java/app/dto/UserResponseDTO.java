package app.dto;

import app.entities.ExperienceLevel;
import app.entities.TrainingGoal;

public record UserResponseDTO(int id, String name, String email, int age, double height, double weight, ExperienceLevel experienceLevel, int trainingDaysPerWeek, TrainingGoal goal, Integer workoutProgramId) {
}
