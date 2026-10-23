package app.services;

import app.dao.UserDAO;
import app.entities.User;
import app.exceptions.ApiException;
import jakarta.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;

import java.util.Locale;

public class AuthService {

    private final UserDAO userDAO;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthService(UserDAO userDAO, PasswordService passwordService, JwtService jwtService) {
        this.userDAO = userDAO;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    public User register(User user) {

        String email = normalizeEmail(user.getEmail());
        user.setEmail(email);
        User existingUser = userDAO.getByEmail(email);

        if (existingUser != null) {
            throw new ApiException(409, "Email already in use");
        }

        String hashedPassword = passwordService.hashPassword(user.getPassword());

        user.setPassword(hashedPassword);

        try {
            return userDAO.create(user);
        } catch (PersistenceException exception) {
            if (isUniqueEmailViolation(exception)) {
                throw new ApiException(409, "Email already in use");
            }
            throw exception;
        }
    }


    public String login(String email, String password) {

        User user = userDAO.getByEmail(normalizeEmail(email));

        if (user == null || user.getPassword() == null || user.getPassword().isBlank()) {
            throw new ApiException(401, "Invalid email or password");
        }

        boolean passwordMatches = passwordService.verifyPassword(password, user.getPassword());

        if (!passwordMatches) {
            throw new ApiException(401, "Invalid email or password");
        }

        return jwtService.generateToken(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isUniqueEmailViolation(Throwable exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolation && "23505".equals(constraintViolation.getSQLState())) {
                return true;
            }
            cause = cause.getCause();
        }

        return false;
    }


}
