package app.api.routes;

import app.api.WorkoutProgramController;
import io.javalin.Javalin;

public class WorkoutProgramRoutes {

    private final WorkoutProgramController controller;


    public WorkoutProgramRoutes(WorkoutProgramController controller) {
        this.controller = controller;
    }


    public void register(Javalin app) {

        app.post(
                "/api/users/{id}/workout-program",
                controller::generateForUser
        );
    }
}
