package app.api.routes;

import app.api.CoachController;
import io.javalin.Javalin;

public class CoachRoutes {

    private final CoachController controller;

    public CoachRoutes(CoachController controller) {
        this.controller = controller;
    }


    public void register(Javalin app) {

        app.post(
                "/api/coach",
                controller::ask
        );
    }
}