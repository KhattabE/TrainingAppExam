package app.services;

import app.dao.UserDAO;
import app.dao.UserDAOImpl;
import app.entities.ExperienceLevel;
import app.entities.TrainingGoal;
import app.entities.User;
import app.exceptions.ApiException;
import app.testutils.DatabaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest extends DatabaseTest {

    private static final String TEST_SECRET = "test-secret-key-that-is-at-least-32-bytes-long";
    private UserDAO userDAO;
    private PasswordService passwordService;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAOImpl(emf);
        passwordService = new PasswordService();
        jwtService = new JwtService(TEST_SECRET);
        authService = new AuthService(userDAO, passwordService, jwtService);
    }

    @Test
    void registerHashesPasswordBeforePersistence() {
        User user = registrationUser("  NEW@Test.DK  ", "plain-password");

        User registered = authService.register(user);
        User stored = userDAO.getById(registered.getId());

        assertEquals("new@test.dk", stored.getEmail());
        assertNotEquals("plain-password", stored.getPassword());
        assertTrue(passwordService.verifyPassword("plain-password", stored.getPassword()));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        User user = registrationUser("  ANNA@TEST.DK ", "plain-password");

        ApiException exception = assertThrows(ApiException.class, () -> authService.register(user));

        assertEquals(409, exception.getCode());
        assertEquals("Email already in use", exception.getMessage());
    }

    @Test
    void registerMapsDatabaseEmailConflictTo409() {
        UserDAO racingUserDAO = new UserDAOImpl(emf) {
            @Override
            public User getByEmail(String email) {
                return null;
            }
        };
        AuthService racingAuthService = new AuthService(racingUserDAO, passwordService, jwtService);

        ApiException exception = assertThrows(ApiException.class, () -> racingAuthService.register(registrationUser(seed.beginner().getEmail(), "plain-password")));

        assertEquals(409, exception.getCode());
        assertEquals("Email already in use", exception.getMessage());
    }

    @Test
    void loginReturnsValidTokenForCorrectPassword() {
        authService.register(registrationUser("login@test.dk", "correct-password"));

        String token = authService.login("  LOGIN@Test.DK ", "correct-password");

        assertTrue(jwtService.validateToken(token));
        assertEquals("login@test.dk", jwtService.getEmailFromToken(token));
    }

    @Test
    void loginRejectsWrongPassword() {
        authService.register(registrationUser("login@test.dk", "correct-password"));

        ApiException exception = assertThrows(ApiException.class, () -> authService.login("login@test.dk", "wrong-password"));

        assertEquals(401, exception.getCode());
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void loginRejectsUnknownEmailWithSameMessage() {
        ApiException exception = assertThrows(ApiException.class, () -> authService.login("unknown@test.dk", "password"));

        assertEquals(401, exception.getCode());
        assertEquals("Invalid email or password", exception.getMessage());
    }

    private User registrationUser(String email, String password) {
        User user = new User("Test User", email, 25, 180, 80, ExperienceLevel.BEGINNER, 3, TrainingGoal.MUSCLE_GAIN);
        user.setPassword(password);
        return user;
    }
}
