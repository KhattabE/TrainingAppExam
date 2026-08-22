package app.config;

import app.entities.Study;
import org.hibernate.cfg.Configuration;

final class EntityRegistry {

    private EntityRegistry() {}

    static void registerEntities(Configuration configuration) {
        configuration.addAnnotatedClass(Study.class);
        // TODO: Add more entities here...
    }
}