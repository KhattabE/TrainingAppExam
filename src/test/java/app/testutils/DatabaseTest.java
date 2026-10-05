package app.testutils;

import app.config.HibernateTestConfig;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class DatabaseTest {

    private final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    protected EntityManagerFactory emf;
    protected TestData.Seed seed;

    @BeforeAll
    void startDatabase() {
        postgres.start();
        try {
            emf = HibernateTestConfig.createEntityManagerFactory(postgres);
        } catch (RuntimeException | Error exception) {
            postgres.stop();
            throw exception;
        }
    }

    @BeforeEach
    void resetDatabase() {
        seed = TestData.populate(emf);
    }

    @AfterAll
    void closeDatabase() {
        try {
            if (emf != null) {
                emf.close();
            }
        } finally {
            postgres.stop();
        }
    }
}
