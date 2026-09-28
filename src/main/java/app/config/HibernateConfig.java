package app.config;

import app.utils.Utils;
import jakarta.persistence.EntityManagerFactory;

import java.util.Properties;

public final class HibernateConfig {

    private static volatile EntityManagerFactory emf;

    private HibernateConfig() {}

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            synchronized (HibernateConfig.class) {
                if (emf == null) {
                    emf = HibernateEmfBuilder.build(buildProps());
                }
            }
        }
        return emf;
    }

    private static Properties buildProps() {
        Properties props = HibernateBaseProperties.createBase();

        props.put("hibernate.hbm2ddl.auto", "update");

        if (System.getenv("DEPLOYED") != null) {
            setDeployedProperties(props);
        } else {
            setDevProperties(props);
        }
        return props;
    }

    private static void setDeployedProperties(Properties props) {
        String dbName = System.getenv("DB_NAME");
        props.setProperty("hibernate.connection.url", System.getenv("CONNECTION_STR") + dbName);
        props.setProperty("hibernate.connection.username", System.getenv("DB_USERNAME"));
        props.setProperty("hibernate.connection.password", System.getenv("JDBC_PASSWORD"));
    }

    private static void setDevProperties(Properties props) {
        String dbName = Utils.getPropertyValue("DB_NAME", "config.properties");
        String username = Utils.getPropertyValue("DB_USERNAME", "config.properties");
        String passwordVariable = Utils.getPropertyValue("DB_PASSWORD", "config.properties");
        String password = System.getenv(passwordVariable);

        props.put("hibernate.connection.url", "jdbc:postgresql://134.209.233.109:5432/" + dbName);
        props.put("hibernate.connection.username", username);
        props.put("hibernate.connection.password", password);
    }
}