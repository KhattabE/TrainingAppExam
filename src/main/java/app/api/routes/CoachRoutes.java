package app.api.routes;

import app.api.CoachController;
import io.javalin.config.RoutesConfig;

public class CoachRoutes {

    private final CoachController controller;

    public CoachRoutes(CoachController controller) {
        this.controller = controller;
    }

    public void register(RoutesConfig routes) {
        routes.post("/api/coach", controller::ask);
    }
}
