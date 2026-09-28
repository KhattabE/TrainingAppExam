package app.api.routes;

import app.api.UserController;
import io.javalin.Javalin;

public class UserRoutes {

    private final UserController controller;

    public UserRoutes(UserController controller) {
        this.controller = controller;
    }

    public void register(Javalin app) {
        app.post("/api/users", controller::create);
        app.get("/api/users", controller::getAll);
        app.get("/api/users/{id}", controller::getById);
    }
}
