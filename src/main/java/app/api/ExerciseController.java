package app.api;

import app.dao.ExerciseDAO;
import app.dto.ExerciseResponseDTO;
import app.entities.Exercise;
import app.exceptions.ApiException;
import io.javalin.http.Context;

import java.util.List;

public class ExerciseController {

    private final ExerciseDAO exerciseDAO;

    public ExerciseController(ExerciseDAO exerciseDAO) {
        this.exerciseDAO = exerciseDAO;
    }

    public void getAll(Context ctx) {
        List<ExerciseResponseDTO> exercises = exerciseDAO.getAll()
                .stream()
                .map(this::toResponse)
                .toList();

        ctx.status(200);
        ctx.json(exercises);
    }

    public void getById(Context ctx) {
        int id = getId(ctx);
        Exercise exercise = exerciseDAO.getById(id);

        if (exercise == null) {
            throw new ApiException(404, "Exercise not found");
        }

        ctx.status(200);
        ctx.json(toResponse(exercise));
    }

    private int getId(Context ctx) {
        return ctx.pathParamAsClass("id", Integer.class)
                .check(id -> id > 0, "Id must be greater than 0")
                .get();
    }

    private ExerciseResponseDTO toResponse(Exercise exercise) {
        return new ExerciseResponseDTO(
                exercise.getId(),
                exercise.getName(),
                exercise.getMuscleGroup(),
                exercise.getSets(),
                exercise.getReps()
        );
    }
}
