package app.dto;

import app.entities.MuscleGroup;

public record GeneratedExerciseDTO(String name, MuscleGroup muscleGroup, int sets, int reps) {
}