package app.config;

import jakarta.persistence.EntityManagerFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Properties;

public final class HibernateTestConfig {

    private HibernateTestConfig() {}

    public static EntityManagerFactory createEntityManagerFactory(PostgreSQLContainer postgres) {
        Properties props = HibernateBaseProperties.createBase();
        props.put("hibernate.connection.url", postgres.getJdbcUrl());
        props.put("hibernate.connection.username", postgres.getUsername());
        props.put("hibernate.connection.password", postgres.getPassword());
        props.put("hibernate.hbm2ddl.auto", "create-drop");
        return HibernateEmfBuilder.build(props);
    }
}
