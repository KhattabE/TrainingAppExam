package app.dao;

import app.entities.Exercise;
import app.entities.MuscleGroup;
import app.testutils.DatabaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExerciseDAOTest extends DatabaseTest {

    private ExerciseDAO exerciseDAO;

    @BeforeEach
    void setUp() {
        exerciseDAO = new ExerciseDAOImpl(emf);
    }

    @Test
    void createAndReadExercise() {
        Exercise exercise = new Exercise("Deadlift", MuscleGroup.BACK, 3, 5);
        Exercise saved = exerciseDAO.create(exercise);
        Exercise found = exerciseDAO.getById(saved.getId());
        assertNotNull(saved.getId());
        assertEquals("Deadlift", found.getName());
        assertEquals(MuscleGroup.BACK, found.getMuscleGroup());
        assertEquals(3, found.getSets());
        assertEquals(5, found.getReps());
    }

    @Test
    void listContainsExactlySeededExercises() {
        List<Exercise> exercises = exerciseDAO.getAll();
        assertEquals(2, exercises.size());
        assertTrue(exercises.stream().anyMatch(exercise -> exercise.getId().equals(seed.benchPress().getId())));
        assertTrue(exercises.stream().anyMatch(exercise -> exercise.getId().equals(seed.squat().getId())));
    }

    @Test
    void updateIsPersisted() {
        Exercise exercise = exerciseDAO.getById(seed.benchPress().getId());
        exercise.setName("Incline Bench Press");
        exercise.setSets(4);
        exercise.setReps(8);
        exerciseDAO.update(exercise);
        Exercise found = exerciseDAO.getById(exercise.getId());
        assertEquals("Incline Bench Press", found.getName());
        assertEquals(4, found.getSets());
        assertEquals(8, found.getReps());
    }

    @Test
    void deleteRemovesUnassignedExercise() {
        Exercise exercise = exerciseDAO.create(new Exercise("Curl", MuscleGroup.BICEPS, 3, 12));
        exerciseDAO.delete(exercise.getId());
        assertNull(exerciseDAO.getById(exercise.getId()));
        assertEquals(2, exerciseDAO.getAll().size());
    }

    @Test
    void muscleGroupQueryReturnsOnlyMatchingExercise() {
        List<Exercise> exercises = exerciseDAO.getByMuscleGroup(MuscleGroup.CHEST);
        assertEquals(1, exercises.size());
        assertEquals(seed.benchPress().getId(), exercises.get(0).getId());
        assertTrue(exerciseDAO.getByMuscleGroup(MuscleGroup.CORE).isEmpty());
    }
}
