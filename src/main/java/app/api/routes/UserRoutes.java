package app.api.routes;

import app.api.UserController;
import io.javalin.config.RoutesConfig;

public class UserRoutes {

    private final UserController controller;

    public UserRoutes(UserController controller) {
        this.controller = controller;
    }

    public void register(RoutesConfig routes) {
        routes.get("/api/users/me", controller::getCurrentUser);
        routes.put("/api/users/me", controller::updateCurrentUser);
    }
}
