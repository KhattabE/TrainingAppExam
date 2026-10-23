package app.api.routes;

import app.api.AuthController;
import io.javalin.config.RoutesConfig;

public class AuthRoutes {

    private final AuthController controller;

    public AuthRoutes(AuthController controller) {
        this.controller = controller;
    }

    public void register(RoutesConfig routes) {
        routes.post("/auth/register", controller::register);
        routes.post("/auth/login", controller::login);
    }
}
