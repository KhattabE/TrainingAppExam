package app.api;

import app.dao.ExerciseDAOImpl;
import app.dto.ExerciseRequestDTO;
import app.dto.ExerciseResponseDTO;
import app.entities.Exercise;
import io.javalin.http.Context;

import java.util.List;

public class ExerciseController {

    private final ExerciseDAOImpl exerciseDAO;

    public ExerciseController(ExerciseDAOImpl exerciseDAO) {
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
            ctx.status(404);
            ctx.json(new ErrorResponse(404, "Exercise not found"));
            return;
        }

        ctx.status(200);
        ctx.json(toResponse(exercise));
    }

    public void create(Context ctx) {
        ExerciseRequestDTO request = getValidatedRequest(ctx);

        Exercise exercise = new Exercise(
                request.name(),
                request.muscleGroup(),
                request.sets(),
                request.reps()
        );

        Exercise savedExercise = exerciseDAO.create(exercise);

        ctx.status(201);
        ctx.json(toResponse(savedExercise));
    }

    public void update(Context ctx) {
        int id = getId(ctx);
        Exercise exercise = exerciseDAO.getById(id);

        if (exercise == null) {
            ctx.status(404);
            ctx.json(new ErrorResponse(404, "Exercise not found"));
            return;
        }

        ExerciseRequestDTO request = getValidatedRequest(ctx);

        exercise.setName(request.name());
        exercise.setMuscleGroup(request.muscleGroup());
        exercise.setSets(request.sets());
        exercise.setReps(request.reps());

        Exercise updatedExercise = exerciseDAO.update(exercise);

        ctx.status(200);
        ctx.json(toResponse(updatedExercise));
    }

    public void delete(Context ctx) {
        int id = getId(ctx);
        Exercise exercise = exerciseDAO.getById(id);

        if (exercise == null) {
            ctx.status(404);
            ctx.json(new ErrorResponse(404, "Exercise not found"));
            return;
        }

        exerciseDAO.delete(id);
        ctx.status(204);
    }

    private int getId(Context ctx) {
        return ctx.pathParamAsClass("id", Integer.class)
                .check(id -> id > 0, "Id must be greater than 0")
                .get();
    }

    private ExerciseRequestDTO getValidatedRequest(Context ctx) {
        return ctx.bodyValidator(ExerciseRequestDTO.class)
                .check(request -> request.name() != null && !request.name().isBlank(), "Name is required")
                .check(request -> request.muscleGroup() != null, "Muscle group is required")
                .check(request -> request.sets() > 0, "Sets must be greater than 0")
                .check(request -> request.reps() > 0, "Reps must be greater than 0")
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