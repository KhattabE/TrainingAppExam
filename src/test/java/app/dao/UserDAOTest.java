package app.dao;

import app.entities.ExperienceLevel;
import app.entities.TrainingGoal;
import app.entities.User;
import app.testutils.DatabaseTest;
import jakarta.persistence.PersistenceException;
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
        user.setPassword("test-password");
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
    void duplicateEmailIsRejectedByDatabase() {
        User user = new User("Duplicate", seed.beginner().getEmail(), 25, 170, 65, ExperienceLevel.BEGINNER, 3, TrainingGoal.MUSCLE_GAIN);
        user.setPassword("test-password");

        assertThrows(PersistenceException.class, () -> userDAO.create(user));
    }

    @Test
    void nullPasswordIsRejectedByDatabase() {
        User user = new User("No Password", "no-password@test.dk", 25, 170, 65, ExperienceLevel.BEGINNER, 3, TrainingGoal.MUSCLE_GAIN);

        assertThrows(PersistenceException.class, () -> userDAO.create(user));
    }

    @Test
    void emailLookupFindsLegacyCaseAndWhitespaceVariant() {
        User legacyUser = new User("Legacy", "  LEGACY@Test.DK ", 25, 170, 65, ExperienceLevel.BEGINNER, 3, TrainingGoal.MUSCLE_GAIN);
        legacyUser.setPassword("test-password");
        userDAO.create(legacyUser);

        User found = userDAO.getByEmail("legacy@test.dk");

        assertEquals(legacyUser.getId(), found.getId());
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
    void profileUpdatePreservesAWorkoutProgramChangedAfterTheProfileWasRead() {
        User profile = userDAO.getById(seed.beginner().getId());
        User userWithNewProgram = userDAO.getById(seed.beginner().getId());
        userWithNewProgram.setWorkoutProgram(seed.upperBody());
        userDAO.update(userWithNewProgram);
        profile.setName("Anna Updated");

        userDAO.updateProfile(profile);

        User found = userDAO.getById(profile.getId());
        assertEquals("Anna Updated", found.getName());
        assertEquals(seed.upperBody().getId(), found.getWorkoutProgram().getId());
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
