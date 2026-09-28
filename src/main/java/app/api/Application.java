package app.api;

import app.api.routes.CoachRoutes;
import app.api.routes.ExerciseRoutes;
import app.api.routes.UserRoutes;
import app.api.routes.WorkoutProgramRoutes;
import app.dao.ExerciseDAOImpl;
import app.dao.UserDAOImpl;
import app.dao.WorkoutProgramDAOImpl;
import app.services.AiCoachService;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {

        AiCoachService aiCoachService = new AiCoachService();

        UserDAOImpl userDAO = new UserDAOImpl();
        ExerciseDAOImpl exerciseDAO = new ExerciseDAOImpl();
        WorkoutProgramDAOImpl workoutProgramDAO = new WorkoutProgramDAOImpl();

        CoachController coachController = new CoachController(aiCoachService);
        ExerciseController exerciseController = new ExerciseController(exerciseDAO);
        UserController userController = new UserController(userDAO);

        WorkoutProgramController workoutProgramController = new WorkoutProgramController(aiCoachService, userDAO, exerciseDAO, workoutProgramDAO);

        CoachRoutes coachRoutes = new CoachRoutes(coachController);
        ExerciseRoutes exerciseRoutes = new ExerciseRoutes(exerciseController);
        UserRoutes userRoutes = new UserRoutes(userController);
        WorkoutProgramRoutes workoutProgramRoutes = new WorkoutProgramRoutes(workoutProgramController);

        Javalin app = Javalin.create();

        app.exception(Exception.class, (exception, ctx) -> {
            logger.error("Unhandled error on {} {}", ctx.method(), ctx.path(), exception);

            ctx.status(500);
            ctx.json(new ErrorResponse(500, "Internal server error"));
        });

        app.before(ctx -> {
            logger.info("{} {}", ctx.method(), ctx.path());
        });

        app.after(ctx -> {
            logger.info("{} {} -> {}", ctx.method(), ctx.path(), ctx.status());
        });

        app.get("/api/health", ctx -> {
            ctx.json(Map.of(
                    "status", "ok",
                    "message", "API is running"
            ));
        });

        coachRoutes.register(app);
        exerciseRoutes.register(app);
        userRoutes.register(app);
        workoutProgramRoutes.register(app);

        app.start(7070);
    }
}