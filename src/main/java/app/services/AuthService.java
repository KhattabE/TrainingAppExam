package app.services;

import app.dao.UserDAO;
import app.entities.User;

public class AuthService {

    private final UserDAO userDAO;
    private final PasswordService passwordService;

    public AuthService(UserDAO userDAO, PasswordService passwordService) {
        this.userDAO = userDAO;
        this.passwordService = passwordService;
    }

    public User register(User user) {

        User existingUser = userDAO.getByEmail(user.getEmail());

        if (existingUser != null) {
            throw new RuntimeException("Email already in use");
        }

        String hashedPassword = passwordService.hashPassword(user.getPassword());

        user.setPassword(hashedPassword);

        return userDAO.create(user);
    }


    public User login(String email, String password) {

        User user = userDAO.getByEmail(email);

        if (user == null) {
            throw new RuntimeException("Invalid email or password");
        }

        boolean passwordMatches = passwordService.verifyPassword(password, user.getPassword());

        if (!passwordMatches) {
            throw new RuntimeException("Invalid email or password");
        }

        return user;
    }



}