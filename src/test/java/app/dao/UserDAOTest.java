package app.dao;

import app.entities.ExperienceLevel;
import app.entities.TrainingGoal;
import app.entities.User;
import app.testutils.DatabaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest extends DatabaseTest {

    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAOImpl(emf);
    }

    @Test
    void createAndReadUser() {
        User user = new User("Mia", "mia@test.dk", 24, 165, 60, ExperienceLevel.INTERMEDIATE, 2, TrainingGoal.GENERAL_FITNESS);
        User saved = userDAO.create(user);
        User found = userDAO.getById(saved.getId());
        assertTrue(saved.getId() > 0);
        assertEquals("Mia", found.getName());
        assertEquals("mia@test.dk", found.getEmail());
        assertEquals(24, found.getAge());
        assertEquals(165, found.getHeight());
        assertEquals(60, found.getWeight());
        assertEquals(ExperienceLevel.INTERMEDIATE, found.getExperienceLevel());
        assertEquals(2, found.getTrainingDaysPerWeek());
        assertEquals(TrainingGoal.GENERAL_FITNESS, found.getGoal());
        assertNull(found.getWorkoutProgram());
    }

    @Test
    void listContainsExactlySeededUsers() {
        List<User> users = userDAO.getAll();
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(user -> user.getId() == seed.beginner().getId()));
        assertTrue(users.stream().anyMatch(user -> user.getId() == seed.advanced().getId()));
    }

    @Test
    void updateIsPersisted() {
        User user = userDAO.getById(seed.beginner().getId());
        user.setName("Anna Updated");
        user.setWeight(67);
        userDAO.update(user);
        User found = userDAO.getById(user.getId());
        assertEquals("Anna Updated", found.getName());
        assertEquals(67, found.getWeight());
        assertEquals(seed.fullBody().getId(), found.getWorkoutProgram().getId());
    }

    @Test
    void deletePreservesSharedWorkoutProgram() {
        userDAO.delete(seed.beginner().getId());
        assertNull(userDAO.getById(seed.beginner().getId()));
        assertNotNull(new WorkoutProgramDAOImpl(emf).getById(seed.fullBody().getId()));
        assertEquals(1, userDAO.getAll().size());
    }

    @Test
    void experienceQueryReturnsOnlyMatchingUser() {
        List<User> users = userDAO.getByExperienceLevel(ExperienceLevel.BEGINNER);
        assertEquals(1, users.size());
        assertEquals(seed.beginner().getId(), users.get(0).getId());
        assertTrue(userDAO.getByExperienceLevel(ExperienceLevel.INTERMEDIATE).isEmpty());
    }

    @Test
    void workoutProgramQueryReturnsOnlyAssignedUser() {
        List<User> users = userDAO.getByWorkoutProgramName("Full Body");
        assertEquals(1, users.size());
        assertEquals(seed.beginner().getId(), users.get(0).getId());
        assertTrue(userDAO.getByWorkoutProgramName("Unknown program").isEmpty());
    }
}
