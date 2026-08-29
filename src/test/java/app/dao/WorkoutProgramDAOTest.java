package app.dao;

import app.entities.WorkoutProgram;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WorkoutProgramDAOTest {

    private WorkoutProgramDAO workoutProgramDAO = new WorkoutProgramDAOImpl();

    @Test
    void createAndGetByIdTest() {
        WorkoutProgram workoutProgram =
                new WorkoutProgram("Test Full Body", "Test program", 3);

        workoutProgramDAO.create(workoutProgram);

        WorkoutProgram foundWorkoutProgram =
                workoutProgramDAO.getById(workoutProgram.getId());

        assertNotNull(foundWorkoutProgram);
        assertEquals("Test Full Body", foundWorkoutProgram.getName());

        workoutProgramDAO.delete(workoutProgram.getId());
    }

    @Test
    void getByTrainingDaysPerWeekTest() {
        WorkoutProgram workoutProgram =
                new WorkoutProgram("Test Program 4 Days", "Test program", 4);

        workoutProgramDAO.create(workoutProgram);

        List<WorkoutProgram> programs =
                workoutProgramDAO.getByTrainingDaysPerWeek(4);

        assertFalse(programs.isEmpty());

        workoutProgramDAO.delete(workoutProgram.getId());
    }
}