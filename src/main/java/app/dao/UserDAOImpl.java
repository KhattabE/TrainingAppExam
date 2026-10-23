package app.dao;

import app.entities.ExperienceLevel;
import app.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Locale;

public class UserDAOImpl implements UserDAO {

    private final EntityManagerFactory emf;

    public UserDAOImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public User create(User user) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        }
        return user;
    }

    @Override
    public User getById(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(User.class, id);
        }
    }

    @Override
    public User update(User user) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            User updatedUser = em.merge(user);
            em.getTransaction().commit();
            return updatedUser;
        }
    }

    @Override
    public User updateProfile(User user) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            User currentUser = em.find(User.class, user.getId(), LockModeType.PESSIMISTIC_WRITE);

            if (currentUser == null) {
                em.getTransaction().rollback();
                return null;
            }

            currentUser.setName(user.getName());
            currentUser.setAge(user.getAge());
            currentUser.setHeight(user.getHeight());
            currentUser.setWeight(user.getWeight());
            currentUser.setExperienceLevel(user.getExperienceLevel());
            currentUser.setTrainingDaysPerWeek(user.getTrainingDaysPerWeek());
            currentUser.setGoal(user.getGoal());
            em.getTransaction().commit();
            return currentUser;
        }
    }

    @Override
    public void delete(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            User user = em.find(User.class, id);

            if (user != null) {
                em.remove(user);
            }

            em.getTransaction().commit();
        }
    }

    @Override
    public List<User> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            List<User> allUsers = em.createQuery("SELECT u FROM User u", User.class)
                    .getResultList();
            return allUsers;
        }
    }

    @Override
    public List<User> getByExperienceLevel(ExperienceLevel experienceLevel) {
        try (EntityManager em = emf.createEntityManager()) {
            List<User> users = em.createQuery("SELECT u FROM User u WHERE u.experienceLevel = :experienceLevel", User.class)
                    .setParameter("experienceLevel", experienceLevel)
                    .getResultList();
            return users;
        }
    }

    @Override
    public List<User> getByWorkoutProgramName(String programName) {
        try (EntityManager em = emf.createEntityManager()) {
            List<User> users = em.createQuery("SELECT u FROM User u WHERE u.workoutProgram.name = :programName", User.class)
                    .setParameter("programName", programName)
                    .getResultList();
            return users;
        }
    }

    @Override
    public User getByEmail(String email) {
        if (email == null) {
            return null;
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT u FROM User u WHERE LOWER(TRIM(u.email)) = :email", User.class)
                    .setParameter("email", normalizedEmail)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }
}
