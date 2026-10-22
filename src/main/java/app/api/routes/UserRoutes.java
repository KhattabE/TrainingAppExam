package app.api.routes;

import app.api.UserController;
import io.javalin.Javalin;

public class UserRoutes {

    private final UserController controller;

    public UserRoutes(UserController controller) {
        this.controller = controller;
    }

    public void register(Javalin app) {
        app.get("/api/users/me", controller::getCurrentUser);
    }
}
