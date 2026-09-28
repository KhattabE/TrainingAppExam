package app;


import app.config.HibernateConfig;
import app.entities.User;
import jakarta.persistence.EntityManagerFactory;

public class Main {
       public static void main(String[] args) {

           EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

           User user = new User();
           
           emf.close();

    }
}
