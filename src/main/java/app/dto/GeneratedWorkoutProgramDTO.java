package app.dto;

import java.util.List;

public record GeneratedWorkoutProgramDTO(String name, String description, List<GeneratedExerciseDTO> exercises) {
}
