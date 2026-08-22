package app.dao;

import app.config.HibernateConfig;
import app.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class UserDAOImpl implements UserDAO{

    private EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();


    @Override
    public User create(User user) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
        em.close();
        return user;
    }

    @Override
    public User getById(int id) {
        EntityManager em = emf.createEntityManager();
        User foundUser = em.find(User.class, id);
        em.close();
        return foundUser;
    }

    @Override
    public User update(User user) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        User updatedUser = em.merge(user);
        em.getTransaction().commit();
        em.close();
        return updatedUser;
    }

    @Override
    public void delete(int id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        User user = em.find(User.class, id);
        if (user != null) {
            em.remove(user);
        }
        em.getTransaction().commit();
        em.close();
    }
}
