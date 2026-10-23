package app.api.routes;

import app.api.WorkoutProgramController;
import io.javalin.config.RoutesConfig;

public class WorkoutProgramRoutes {

    private final WorkoutProgramController controller;

    public WorkoutProgramRoutes(WorkoutProgramController controller) {
        this.controller = controller;
    }

    public void register(RoutesConfig routes) {
        routes.post("/api/users/{id}/workout-program", controller::generateForUser);
        routes.get("/api/users/{id}/workout-program", controller::getForUser);
    }
}
