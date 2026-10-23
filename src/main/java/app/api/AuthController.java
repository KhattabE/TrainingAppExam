package app.api;

import app.dto.AuthResponseDTO;
import app.dto.LoginRequestDTO;
import app.dto.RegisterRequestDTO;
import app.dto.UserResponseDTO;
import app.entities.User;
import app.services.AuthService;
import io.javalin.http.Context;

public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public void register(Context ctx) {
        RegisterRequestDTO request = ctx.bodyValidator(RegisterRequestDTO.class)
                .check(user -> user.name() != null && !user.name().isBlank(), "Name is required")
                .check(user -> user.name() == null || user.name().length() <= 255, "Name must be at most 255 characters")
                .check(user -> user.email() != null && !user.email().isBlank(), "Email is required")
                .check(user -> user.email() == null || user.email().trim().length() <= 255, "Email must be at most 255 characters")
                .check(user -> user.password() != null && !user.password().isBlank(), "Password is required")
                .check(user -> user.age() > 0, "Age must be greater than 0")
                .check(user -> user.height() > 0, "Height must be greater than 0")
                .check(user -> user.weight() > 0, "Weight must be greater than 0")
                .check(user -> user.experienceLevel() != null, "Experience level is required")
                .check(user -> user.trainingDaysPerWeek() >= 1 && user.trainingDaysPerWeek() <= 7,
                        "Training days must be between 1 and 7")
                .check(user -> user.goal() != null, "Training goal is required")
                .required()
                .get();

        User user = new User(request.name(), request.email(), request.age(), request.height(), request.weight(),
                request.experienceLevel(), request.trainingDaysPerWeek(), request.goal());
        user.setPassword(request.password());

        User savedUser = authService.register(user);
        ctx.status(201).json(toResponse(savedUser));
    }

    public void login(Context ctx) {
        LoginRequestDTO request = ctx.bodyValidator(LoginRequestDTO.class)
                .check(login -> login.email() != null && !login.email().isBlank(), "Email is required")
                .check(login -> login.password() != null && !login.password().isBlank(), "Password is required")
                .required()
                .get();

        String token = authService.login(request.email(), request.password());
        ctx.status(200).json(new AuthResponseDTO(token));
    }

    private UserResponseDTO toResponse(User user) {
        Integer workoutProgramId = null;

        if (user.getWorkoutProgram() != null) {
            workoutProgramId = user.getWorkoutProgram().getId();
        }

        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getAge(), user.getHeight(),
                user.getWeight(), user.getExperienceLevel(), user.getTrainingDaysPerWeek(), user.getGoal(),
                workoutProgramId);
    }
}
