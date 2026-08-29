package app.dao;

import app.entities.Exercise;
import app.entities.MuscleGroup;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExerciseDAOTest {

    private ExerciseDAO exerciseDAO = new ExerciseDAOImpl();

    @Test
    void createAndGetByIdTest() {
        Exercise exercise =
                new Exercise("Test Bench Press", MuscleGroup.CHEST, 3, 10);

        exerciseDAO.create(exercise);

        Exercise foundExercise =
                exerciseDAO.getById(exercise.getId());

        assertNotNull(foundExercise);
        assertEquals("Test Bench Press", foundExercise.getName());

        exerciseDAO.delete(exercise.getId());
    }

    @Test
    void getByMuscleGroupTest() {
        Exercise exercise =
                new Exercise("Test Chest Exercise", MuscleGroup.CHEST, 3, 12);

        exerciseDAO.create(exercise);

        List<Exercise> exercises =
                exerciseDAO.getByMuscleGroup(MuscleGroup.CHEST);

        assertFalse(exercises.isEmpty());

        exerciseDAO.delete(exercise.getId());
    }
}