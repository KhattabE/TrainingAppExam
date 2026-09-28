package app.api;

import app.dao.ExerciseDAOImpl;
import app.dao.UserDAOImpl;
import app.dao.WorkoutProgramDAOImpl;

import app.dto.ExerciseResponseDTO;
import app.dto.GeneratedExerciseDTO;
import app.dto.GeneratedWorkoutProgramDTO;
import app.dto.WorkoutProgramResponseDTO;

import app.entities.Exercise;
import app.entities.User;
import app.entities.WorkoutProgram;

import app.services.AiCoachService;

import io.javalin.http.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WorkoutProgramController {

    private final AiCoachService aiCoachService;

    private final UserDAOImpl userDAO;

    private final ExerciseDAOImpl exerciseDAO;

    private final WorkoutProgramDAOImpl workoutProgramDAO;


    public WorkoutProgramController(AiCoachService aiCoachService, UserDAOImpl userDAO, ExerciseDAOImpl exerciseDAO, WorkoutProgramDAOImpl workoutProgramDAO) {

        this.aiCoachService = aiCoachService;

        this.userDAO = userDAO;

        this.exerciseDAO = exerciseDAO;

        this.workoutProgramDAO = workoutProgramDAO;
    }


    public void generateForUser(Context ctx)
            throws IOException, InterruptedException {

        int userId = ctx.pathParamAsClass("id", Integer.class).check(id -> id > 0, "User id must be greater than 0").get();


        User user = userDAO.getById(userId);


        if (user == null) {

            ctx.status(404);

            ctx.json(new ErrorResponse(404, "User not found"));

            return;
        }


        GeneratedWorkoutProgramDTO generatedProgram = aiCoachService.generateWorkoutProgram(user);


        if (generatedProgram.exercises() == null || generatedProgram.exercises().isEmpty()) {

            throw new IllegalStateException("Gemini generated a program with no exercises");

        }


        List<Exercise> savedExercises = new ArrayList<>();


        for (GeneratedExerciseDTO generatedExercise : generatedProgram.exercises()) {

            Exercise exercise = new Exercise(generatedExercise.name(), generatedExercise.muscleGroup(), generatedExercise.sets(), generatedExercise.reps());


            Exercise savedExercise = exerciseDAO.create(exercise);


            savedExercises.add(savedExercise);
        }


        WorkoutProgram workoutProgram = new WorkoutProgram(generatedProgram.name(), generatedProgram.description(), user.getTrainingDaysPerWeek());


        workoutProgram.setExercises(savedExercises);


        WorkoutProgram savedProgram = workoutProgramDAO.create(workoutProgram);


        user.setWorkoutProgram(savedProgram);


        userDAO.update(user);


        ctx.status(201);

        ctx.json(toResponse(savedProgram));
    }


    private WorkoutProgramResponseDTO toResponse(WorkoutProgram workoutProgram) {

        List<ExerciseResponseDTO> exerciseResponses =
                workoutProgram
                        .getExercises()
                        .stream()
                        .map(exercise ->
                                new ExerciseResponseDTO(
                                        exercise.getId(),
                                        exercise.getName(),
                                        exercise.getMuscleGroup(),
                                        exercise.getSets(),
                                        exercise.getReps()
                                )
                        )
                        .toList();


        return new WorkoutProgramResponseDTO(
                workoutProgram.getId(),
                workoutProgram.getName(),
                workoutProgram.getDescription(),
                workoutProgram.getTrainingDaysPerWeek(),
                exerciseResponses
        );
    }
}
