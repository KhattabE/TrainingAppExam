package app.dao;

import app.entities.WorkoutProgram;
import app.testutils.DatabaseTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
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
        program.setExercises(new LinkedHashSet<>(List.of(seed.squat())));
        WorkoutProgram saved = workoutProgramDAO.create(program);
        WorkoutProgram found = workoutProgramDAO.getById(saved.getId());
        assertNotNull(saved.getId());
        assertEquals("Leg Day", found.getName());
        assertEquals("Lower body", found.getDescription());
        assertEquals(2, found.getTrainingDaysPerWeek());
        assertEquals(List.of(seed.squat().getId()), found.getExercises().stream().map(exercise -> exercise.getId()).toList());
    }

    @Test
    void joinTableHasCompositePrimaryKey() {
        try (EntityManager em = emf.createEntityManager()) {
            List<?> columns = em.createNativeQuery("""
                    SELECT column_name
                    FROM information_schema.key_column_usage
                    WHERE table_schema = 'public' AND table_name = 'workoutprogram_exercise'
                    AND constraint_name IN (
                        SELECT constraint_name FROM information_schema.table_constraints
                        WHERE table_schema = 'public' AND table_name = 'workoutprogram_exercise'
                        AND constraint_type = 'PRIMARY KEY'
                    )
                    """).getResultList();

            assertEquals(2, columns.size());
            assertTrue(columns.containsAll(List.of("workoutprogram_id", "exercises_id")));
        }
    }

    @Test
    void duplicateExerciseLinkIsRejectedByDatabase() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            try {
                assertThrows(PersistenceException.class, () -> em.createNativeQuery("INSERT INTO workoutprogram_exercise (workoutprogram_id, exercises_id) VALUES (:programId, :exerciseId)")
                        .setParameter("programId", seed.fullBody().getId())
                        .setParameter("exerciseId", seed.benchPress().getId())
                        .executeUpdate());
            } finally {
                em.getTransaction().rollback();
            }
        }
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
