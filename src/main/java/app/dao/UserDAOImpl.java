package app.dao;

import app.config.HibernateConfig;
import app.entities.ExperienceLevel;
import app.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class UserDAOImpl implements UserDAO{

    private EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();


    @Override
    public User create(User user) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        }
        return user;
    }

    @Override
    public User getById(int id) {
        try(EntityManager em = emf.createEntityManager()){
            return em.find(User.class, id);
        }
    }

    @Override
    public User update(User user) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            User updatedUser = em.merge(user);
            em.getTransaction().commit();
            return updatedUser;
        }
    }

    @Override
    public void delete(int id) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            User user = em.find(User.class, id);

            if(user != null){
                em.remove(user);
            }

            em.getTransaction().commit();
        }
    }

    @Override
    public List<User> getAll() {
        try(EntityManager em = emf.createEntityManager()){
            List<User> allUsers = em.createQuery("SELECT u FROM User u", User.class)
                    .getResultList();
            return allUsers;
        }
    }

    @Override
    public List<User> getByExperienceLevel(ExperienceLevel experienceLevel) {
        try(EntityManager em = emf.createEntityManager()){
            List<User> users = em.createQuery("SELECT u FROM User u WHERE u.experienceLevel = :experienceLevel", User.class)
                    .setParameter("experienceLevel", experienceLevel)
                    .getResultList();
            return users;
        }
    }

    @Override
    public List<User> getByWorkoutProgramName(String programName) {
        try(EntityManager em = emf.createEntityManager()){
            List<User> users = em.createQuery("SELECT u FROM User u WHERE u.workoutProgram.name = :programName", User.class)
                    .setParameter("programName", programName)
                    .getResultList();
            return users;
        }
    }
}