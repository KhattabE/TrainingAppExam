package app.dto;

import app.entities.MuscleGroup;

public record ExerciseResponseDTO(Integer id, String name, MuscleGroup muscleGroup, int sets, int reps) {
}
