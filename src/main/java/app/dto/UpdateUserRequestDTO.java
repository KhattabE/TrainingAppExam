package app.dto;

import app.entities.ExperienceLevel;
import app.entities.TrainingGoal;

public record UpdateUserRequestDTO(String name, int age, double height, double weight, ExperienceLevel experienceLevel, int trainingDaysPerWeek, TrainingGoal goal) {
}
