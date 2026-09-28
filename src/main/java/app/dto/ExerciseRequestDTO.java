package app.dto;

import app.entities.MuscleGroup;

public record ExerciseRequestDTO(String name, MuscleGroup muscleGroup, int sets, int reps) {
}