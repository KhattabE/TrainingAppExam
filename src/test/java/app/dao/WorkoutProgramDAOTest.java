package app.dao;

import app.entities.WorkoutProgram;
import app.testutils.DatabaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkoutProgramDAOTest extends DatabaseTest {

    private WorkoutProgramDAO workoutProgramDAO;

    @BeforeEach
    void setUp() {
        workoutProgramDAO = new WorkoutProgramDAOImpl(emf);
    }

    @Test
    void createAndReadProgramWithExercises() {
        WorkoutProgram program = new WorkoutProgram("Leg Day", "Lower body", 2);
        program.setExercises(new ArrayList<>(List.of(seed.squat())));
        WorkoutProgram saved = workoutProgramDAO.create(program);
        WorkoutProgram found = workoutProgramDAO.getById(saved.getId());
        assertNotNull(saved.getId());
        assertEquals("Leg Day", found.getName());
        assertEquals("Lower body", found.getDescription());
        assertEquals(2, found.getTrainingDaysPerWeek());
        assertEquals(List.of(seed.squat().getId()), found.getExercises().stream().map(exercise -> exercise.getId()).toList());
    }

    @Test
    void listContainsExactlySeededPrograms() {
        List<WorkoutProgram> programs = workoutProgramDAO.getAll();
        assertEquals(2, programs.size());
        assertTrue(programs.stream().anyMatch(program -> program.getId().equals(seed.fullBody().getId())));
        assertTrue(programs.stream().anyMatch(program -> program.getId().equals(seed.upperBody().getId())));
    }

    @Test
    void updateIsPersisted() {
        WorkoutProgram program = workoutProgramDAO.getById(seed.upperBody().getId());
        program.setName("Upper Body Updated");
        program.setTrainingDaysPerWeek(5);
        workoutProgramDAO.update(program);
        WorkoutProgram found = workoutProgramDAO.getById(program.getId());
        assertEquals("Upper Body Updated", found.getName());
        assertEquals(5, found.getTrainingDaysPerWeek());
        assertEquals(1, found.getExercises().size());
    }

    @Test
    void deletePreservesExercises() {
        workoutProgramDAO.delete(seed.upperBody().getId());
        assertNull(workoutProgramDAO.getById(seed.upperBody().getId()));
        assertNotNull(new ExerciseDAOImpl(emf).getById(seed.benchPress().getId()));
        assertEquals(1, workoutProgramDAO.getAll().size());
    }

    @Test
    void trainingDaysQueryReturnsOnlyMatchingProgram() {
        List<WorkoutProgram> programs = workoutProgramDAO.getByTrainingDaysPerWeek(3);
        assertEquals(1, programs.size());
        assertEquals(seed.fullBody().getId(), programs.get(0).getId());
        assertTrue(workoutProgramDAO.getByTrainingDaysPerWeek(7).isEmpty());
    }
}
