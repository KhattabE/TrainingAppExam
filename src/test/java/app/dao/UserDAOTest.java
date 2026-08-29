package app.dao;

import app.entities.ExperienceLevel;
import app.entities.TrainingGoal;
import app.entities.User;
import app.entities.WorkoutProgram;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest {

    private UserDAO userDAO = new UserDAOImpl();
    private WorkoutProgramDAO workoutProgramDAO = new WorkoutProgramDAOImpl();

    @Test
    void createAndGetByIdTest() {
        User user = new User(
                "Test User",
                "test@test.dk",
                25,
                180,
                80,
                ExperienceLevel.BEGINNER,
                3,
                TrainingGoal.MUSCLE_GAIN
        );

        userDAO.create(user);

        User foundUser = userDAO.getById(user.getId());

        assertNotNull(foundUser);
        assertEquals("Test User", foundUser.getName());

        userDAO.delete(user.getId());
    }

    @Test
    void getByWorkoutProgramNameTest() {
        WorkoutProgram workoutProgram =
                new WorkoutProgram("Test PPL", "Test program", 6);

        workoutProgramDAO.create(workoutProgram);

        User user = new User(
                "Program Test User",
                "programtest@test.dk",
                25,
                180,
                80,
                ExperienceLevel.INTERMEDIATE,
                6,
                TrainingGoal.MUSCLE_GAIN
        );

        user.setWorkoutProgram(workoutProgram);
        userDAO.create(user);

        List<User> users =
                userDAO.getByWorkoutProgramName("Test PPL");

        assertFalse(users.isEmpty());

        userDAO.delete(user.getId());
        workoutProgramDAO.delete(workoutProgram.getId());
    }
}