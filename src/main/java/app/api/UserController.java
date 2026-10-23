package app.api;

import app.dao.UserDAO;
import app.dto.UpdateUserRequestDTO;
import app.dto.UserResponseDTO;
import app.entities.User;
import app.exceptions.ApiException;
import io.javalin.http.Context;

public class UserController {

    private final UserDAO userDAO;

    public UserController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void getCurrentUser(Context ctx) {
        String email = ctx.attribute("userEmail");
        User user = userDAO.getByEmail(email);

        if (user == null) {
            throw new ApiException(401, "Unauthorized");
        }

        ctx.status(200).json(toResponse(user));
    }

    public void updateCurrentUser(Context ctx) {
        String email = ctx.attribute("userEmail");
        User user = userDAO.getByEmail(email);

        if (user == null) {
            throw new ApiException(401, "Unauthorized");
        }

        UpdateUserRequestDTO request = ctx.bodyValidator(UpdateUserRequestDTO.class)
                .check(update -> update.name() != null && !update.name().isBlank(), "Name is required")
                .check(update -> update.name() == null || update.name().trim().length() <= 255, "Name must be at most 255 characters")
                .check(update -> update.age() > 0, "Age must be greater than 0")
                .check(update -> Double.isFinite(update.height()) && update.height() > 0, "Height must be a finite number greater than 0")
                .check(update -> Double.isFinite(update.weight()) && update.weight() > 0, "Weight must be a finite number greater than 0")
                .check(update -> update.experienceLevel() != null, "Experience level is required")
                .check(update -> update.trainingDaysPerWeek() >= 1 && update.trainingDaysPerWeek() <= 7, "Training days must be between 1 and 7")
                .check(update -> update.goal() != null, "Training goal is required")
                .get();

        user.setName(request.name().trim());
        user.setAge(request.age());
        user.setHeight(request.height());
        user.setWeight(request.weight());
        user.setExperienceLevel(request.experienceLevel());
        user.setTrainingDaysPerWeek(request.trainingDaysPerWeek());
        user.setGoal(request.goal());

        User updatedUser = userDAO.updateProfile(user);

        if (updatedUser == null) {
            throw new ApiException(401, "Unauthorized");
        }

        ctx.status(200).json(toResponse(updatedUser));
    }

    private UserResponseDTO toResponse(User user) {
        Integer workoutProgramId = null;

        if (user.getWorkoutProgram() != null) {
            workoutProgramId = user.getWorkoutProgram().getId();
        }

        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getAge(), user.getHeight(), user.getWeight(), user.getExperienceLevel(), user.getTrainingDaysPerWeek(), user.getGoal(), workoutProgramId);
    }
}
