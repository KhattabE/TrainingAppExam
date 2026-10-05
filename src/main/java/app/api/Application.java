package app.api;

import app.api.routes.CoachRoutes;
import app.api.routes.ExerciseRoutes;
import app.api.routes.UserRoutes;
import app.api.routes.WorkoutProgramRoutes;
import app.config.HibernateConfig;
import app.dao.ExerciseDAOImpl;
import app.dao.UserDAOImpl;
import app.exceptions.ApiException;
import app.services.AiCoachService;
import app.services.WorkoutProgramService;
import io.javalin.Javalin;
import io.javalin.http.HttpResponseException;
import io.javalin.validation.ValidationException;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        Javalin app = createApp(emf, new AiCoachService());
        app.events(events -> events.serverStopped(emf::close));
        app.start(7070);
        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
    }

    public static Javalin createApp(EntityManagerFactory emf, AiCoachService aiCoachService) {
        UserController userController = new UserController(new UserDAOImpl(emf));
        ExerciseController exerciseController = new ExerciseController(new ExerciseDAOImpl(emf));
        CoachController coachController = new CoachController(aiCoachService);
        WorkoutProgramService workoutProgramService = new WorkoutProgramService(emf, aiCoachService);
        WorkoutProgramController workoutProgramController = new WorkoutProgramController(workoutProgramService);
        Javalin app = Javalin.create(config -> config.http.prefer405over404 = true);

        app.exception(ValidationException.class, (exception, ctx) -> {
            String message = exception.getErrors().values().stream()
                    .flatMap(errors -> errors.stream())
                    .map(error -> error.getMessage())
                    .distinct()
                    .collect(Collectors.joining("; "));
            ctx.status(400).json(new ErrorResponse(400, message.isBlank() ? "Invalid request" : message));
        });
        app.exception(HttpResponseException.class, (exception, ctx) -> {
            ctx.status(exception.getStatus()).json(new ErrorResponse(exception.getStatus(), exception.getMessage()));
        });
        app.exception(ApiException.class, (exception, ctx) -> {
            ctx.status(exception.getCode()).json(new ErrorResponse(exception.getCode(), exception.getMessage()));
        });
        app.exception(IOException.class, (exception, ctx) -> {
            logger.error("AI service request failed", exception);
            ctx.status(502).json(new ErrorResponse(502, "AI service could not return a usable response"));
        });
        app.exception(InterruptedException.class, (exception, ctx) -> {
            Thread.currentThread().interrupt();
            ctx.status(503).json(new ErrorResponse(503, "AI request was interrupted"));
        });
        app.exception(Exception.class, (exception, ctx) -> {
            logger.error("Unhandled error on {} {}", ctx.method(), ctx.path(), exception);
            ctx.status(500).json(new ErrorResponse(500, "Internal server error"));
        });

        app.before(ctx -> logger.info("{} {}", ctx.method(), ctx.path()));
        app.after(ctx -> logger.info("{} {} -> {}", ctx.method(), ctx.path(), ctx.status()));
        app.get("/api/health", ctx -> ctx.json(Map.of("status", "ok", "message", "API is running")));

        new CoachRoutes(coachController).register(app);
        new ExerciseRoutes(exerciseController).register(app);
        new UserRoutes(userController).register(app);
        new WorkoutProgramRoutes(workoutProgramController).register(app);
        return app;
    }
}
