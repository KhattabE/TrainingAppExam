package app.api.routes;

import app.api.ExerciseController;
import io.javalin.config.RoutesConfig;

public class ExerciseRoutes {

    private final ExerciseController controller;

    public ExerciseRoutes(ExerciseController controller) {
        this.controller = controller;
    }

    public void register(RoutesConfig routes) {
        routes.get("/api/exercises", controller::getAll);
        routes.get("/api/exercises/{id}", controller::getById);
    }
}
