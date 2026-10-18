package app.services;

import app.dao.UserDAO;
import app.entities.User;
import app.exceptions.ApiException;

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

        User existingUser = userDAO.getByEmail(user.getEmail());

        if (existingUser != null) {
            throw new ApiException(409, "Email already in use");
        }

        String hashedPassword = passwordService.hashPassword(user.getPassword());

        user.setPassword(hashedPassword);

        return userDAO.create(user);
    }


    public String login(String email, String password) {

        User user = userDAO.getByEmail(email);

        if (user == null || user.getPassword() == null || user.getPassword().isBlank()) {
            throw new ApiException(401, "Invalid email or password");
        }

        boolean passwordMatches = passwordService.verifyPassword(password, user.getPassword());

        if (!passwordMatches) {
            throw new ApiException(401, "Invalid email or password");
        }

        return jwtService.generateToken(user);
    }



}
