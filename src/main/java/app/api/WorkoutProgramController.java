
package app.api;

import app.dao.UserDAO;
import app.dto.ExerciseResponseDTO;
import app.dto.WorkoutProgramResponseDTO;
import app.entities.User;
import app.entities.WorkoutProgram;
import app.exceptions.ApiException;
import app.services.WorkoutProgramService;
import io.javalin.http.Context;

import java.io.IOException;
import java.util.List;

public class WorkoutProgramController {

    private final WorkoutProgramService workoutProgramService;
    private final UserDAO userDAO;

    public WorkoutProgramController(WorkoutProgramService workoutProgramService, UserDAO userDAO) {

        this.workoutProgramService = workoutProgramService;
        this.userDAO = userDAO;
    }

    public void generateForUser(Context ctx)
            throws IOException, InterruptedException {

        int userId = ctx.pathParamAsClass("id", Integer.class)
                .check(id -> id > 0, "User id must be greater than 0")
                .get();

        verifyOwnership(ctx, userId);

        WorkoutProgram workoutProgram =
                workoutProgramService.generateForUser(userId);

        ctx.status(201).json(toResponse(workoutProgram));
    }

    public void getForUser(Context ctx) {

        int userId = ctx.pathParamAsClass("id", Integer.class)
                .check(id -> id > 0, "User id must be greater than 0")
                .get();

        verifyOwnership(ctx, userId);

        WorkoutProgram workoutProgram =
                workoutProgramService.getForUser(userId);

        ctx.status(200).json(toResponse(workoutProgram));
    }

    private void verifyOwnership(Context ctx, int requestedUserId) {

        String email = ctx.attribute("userEmail");

        if (email == null) {
            throw new ApiException(401, "Unauthorized");
        }

        User loggedInUser = userDAO.getByEmail(email);

        if (loggedInUser == null) {
            throw new ApiException(401, "Unauthorized");
        }

        if (loggedInUser.getId() != requestedUserId) {
            throw new ApiException(
                    403,
                    "You cannot access another user's workout program"
            );
        }
    }

    private WorkoutProgramResponseDTO toResponse(
            WorkoutProgram workoutProgram) {

        List<ExerciseResponseDTO> exercises =
                workoutProgram.getExercises().stream()
                        .map(exercise -> new ExerciseResponseDTO(exercise.getId(), exercise.getName(), exercise.getMuscleGroup(), exercise.getSets(), exercise.getReps()))
                        .toList();

        return new WorkoutProgramResponseDTO(workoutProgram.getId(), workoutProgram.getName(), workoutProgram.getDescription(), workoutProgram.getTrainingDaysPerWeek(), exercises);
    }
}
