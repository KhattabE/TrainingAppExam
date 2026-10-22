package app.api;

import app.dao.UserDAO;
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

    private UserResponseDTO toResponse(User user) {
        Integer workoutProgramId = null;

        if (user.getWorkoutProgram() != null) {
            workoutProgramId = user.getWorkoutProgram().getId();
        }

        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getAge(), user.getHeight(), user.getWeight(), user.getExperienceLevel(), user.getTrainingDaysPerWeek(), user.getGoal(), workoutProgramId);
    }
}
