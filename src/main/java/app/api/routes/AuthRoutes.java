package app.api.routes;

import app.api.AuthController;
import io.javalin.Javalin;

public class AuthRoutes {

    private final AuthController controller;

    public AuthRoutes(AuthController controller) {
        this.controller = controller;
    }

    public void register(Javalin app) {
        app.post("/auth/register", controller::register);
        app.post("/auth/login", controller::login);
    }
}
