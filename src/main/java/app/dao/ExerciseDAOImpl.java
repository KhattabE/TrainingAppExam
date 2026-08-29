package app.dao;

import app.config.HibernateConfig;
import app.entities.Exercise;
import app.entities.MuscleGroup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class ExerciseDAOImpl implements ExerciseDAO {

    EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    @Override
    public Exercise create(Exercise exercise) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.persist(exercise);
            em.getTransaction().commit();
        }
        return exercise;
    }

    @Override
    public Exercise getById(int id) {
        try(EntityManager em = emf.createEntityManager()){
            return em.find(Exercise.class,id);
        }
    }

    @Override
    public Exercise update(Exercise exercise) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            Exercise updatedExercise = em.merge(exercise);
            em.getTransaction().commit();
            return updatedExercise;
        }
    }

    @Override
    public void delete(int id) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            Exercise foundExercise = em.find(Exercise.class,id);

            if(foundExercise != null){
                em.remove(foundExercise);
            } else {
                System.out.println("No exercise with that id!");
            }
            em.getTransaction().commit();

        }
    }

    @Override
    public List<Exercise> getAll() {
        try(EntityManager em = emf.createEntityManager()){
            List<Exercise> allExercises = em.createQuery("SELECT e FROM Exercise e", Exercise.class)
                    .getResultList();
            return allExercises;
        }
    }

    @Override
    public List<Exercise> getByMuscleGroup(MuscleGroup muscleGroup) {
        try(EntityManager em = emf.createEntityManager()){
            return em.createQuery("SELECT e FROM Exercise e WHERE e.muscleGroup = :muscleGroup", Exercise.class)
                    .setParameter("muscleGroup", muscleGroup)
                    .getResultList();
        }
    }
}