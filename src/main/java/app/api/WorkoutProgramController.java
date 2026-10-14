package app.api;

import app.dto.ExerciseResponseDTO;
import app.dto.WorkoutProgramResponseDTO;
import app.entities.WorkoutProgram;
import app.services.WorkoutProgramService;
import io.javalin.http.Context;

import java.io.IOException;
import java.util.List;

public class WorkoutProgramController {

    private final WorkoutProgramService workoutProgramService;

    public WorkoutProgramController(WorkoutProgramService workoutProgramService) {
        this.workoutProgramService = workoutProgramService;
    }

    public void generateForUser(Context ctx) throws IOException, InterruptedException {
        int userId = ctx.pathParamAsClass("id", Integer.class)
                .check(id -> id > 0, "User id must be greater than 0")
                .get();
        WorkoutProgram workoutProgram = workoutProgramService.generateForUser(userId);
        ctx.status(201).json(toResponse(workoutProgram));
    }

    private WorkoutProgramResponseDTO toResponse(WorkoutProgram workoutProgram) {
        List<ExerciseResponseDTO> exercises = workoutProgram.getExercises().stream()
                .map(exercise -> new ExerciseResponseDTO(exercise.getId(), exercise.getName(),
                        exercise.getMuscleGroup(), exercise.getSets(), exercise.getReps()))
                .toList();
        return new WorkoutProgramResponseDTO(workoutProgram.getId(), workoutProgram.getName(),
                workoutProgram.getDescription(), workoutProgram.getTrainingDaysPerWeek(), exercises);
    }
}
