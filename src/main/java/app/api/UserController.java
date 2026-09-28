package app.api;

import app.dao.UserDAOImpl;
import app.dto.UserRequestDTO;
import app.dto.UserResponseDTO;
import app.entities.User;
import io.javalin.http.Context;

import java.util.List;

public class UserController {

    private final UserDAOImpl userDAO;

    public UserController(UserDAOImpl userDAO) {
        this.userDAO = userDAO;
    }

    public void create(Context ctx) {
        UserRequestDTO request = ctx.bodyValidator(UserRequestDTO.class)
                .check(user -> user.name() != null && !user.name().isBlank(), "Name is required")
                .check(user -> user.email() != null && !user.email().isBlank(), "Email is required")
                .check(user -> user.age() > 0, "Age must be greater than 0")
                .check(user -> user.height() > 0, "Height must be greater than 0")
                .check(user -> user.weight() > 0, "Weight must be greater than 0")
                .check(user -> user.experienceLevel() != null, "Experience level is required")
                .check(user -> user.trainingDaysPerWeek() >= 1 && user.trainingDaysPerWeek() <= 7,
                        "Training days must be between 1 and 7")
                .check(user -> user.goal() != null, "Training goal is required")
                .get();

        User user = new User(
                request.name(),
                request.email(),
                request.age(),
                request.height(),
                request.weight(),
                request.experienceLevel(),
                request.trainingDaysPerWeek(),
                request.goal()
        );

        User savedUser = userDAO.create(user);

        ctx.status(201);
        ctx.json(toResponse(savedUser));
    }

    public void getAll(Context ctx) {
        List<UserResponseDTO> users = userDAO.getAll()
                .stream()
                .map(this::toResponse)
                .toList();

        ctx.status(200);
        ctx.json(users);
    }

    public void getById(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "Id must be greater than 0")
                .get();

        User user = userDAO.getById(id);

        if (user == null) {
            ctx.status(404);
            ctx.json(new ErrorResponse(404, "User not found"));
            return;
        }

        ctx.status(200);
        ctx.json(toResponse(user));
    }

    private UserResponseDTO toResponse(User user) {
        Integer workoutProgramId = null;

        if (user.getWorkoutProgram() != null) {
            workoutProgramId = user.getWorkoutProgram().getId();
        }

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getHeight(),
                user.getWeight(),
                user.getExperienceLevel(),
                user.getTrainingDaysPerWeek(),
                user.getGoal(),
                workoutProgramId
        );
    }
}
