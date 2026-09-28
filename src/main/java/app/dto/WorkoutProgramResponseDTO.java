package app.dto;

import java.util.List;

public record WorkoutProgramResponseDTO(Integer id, String name, String description, int trainingDaysPerWeek, List<ExerciseResponseDTO> exercises) {
}
