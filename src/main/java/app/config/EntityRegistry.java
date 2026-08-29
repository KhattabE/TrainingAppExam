package app.config;


import app.entities.Exercise;
import app.entities.User;
import app.entities.WorkoutProgram;
import org.hibernate.cfg.Configuration;

final class EntityRegistry {

    private EntityRegistry() {}

    static void registerEntities(Configuration configuration) {
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(WorkoutProgram.class);
        configuration.addAnnotatedClass(Exercise.class);
        // TODO: Add more entities here...
    }
}