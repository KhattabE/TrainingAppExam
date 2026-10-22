package app.api.routes;

import app.api.ExerciseController;
import io.javalin.Javalin;

public class ExerciseRoutes {

    private final ExerciseController controller;

    public ExerciseRoutes(ExerciseController controller) {
        this.controller = controller;
    }

    public void register(Javalin app) {
        app.get("/api/exercises", controller::getAll);
        app.get("/api/exercises/{id}", controller::getById);
    }
}
