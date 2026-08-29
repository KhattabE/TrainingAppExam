package app.dao;

import app.config.HibernateConfig;
import app.entities.WorkoutProgram;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class WorkoutProgramDAOImpl implements WorkoutProgramDAO{

       private EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    @Override
    public WorkoutProgram create(WorkoutProgram workoutProgram) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.persist(workoutProgram);
            em.getTransaction().commit();
        }
        return workoutProgram;
    }

    @Override
    public WorkoutProgram getById(int id) {
        try(EntityManager em = emf.createEntityManager()){
            return em.find(WorkoutProgram.class,id);
        }
    }

    @Override
    public WorkoutProgram update(WorkoutProgram workoutProgram) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            WorkoutProgram updatedWorkoutProgram = em.merge(workoutProgram);
            em.getTransaction().commit();
            return updatedWorkoutProgram;
        }
    }

    @Override
    public void delete(int id) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            WorkoutProgram foundWorkout = em.find(WorkoutProgram.class,id);

            if(foundWorkout != null){
                em.remove(foundWorkout);
            } else {
                System.out.println("No workout program with that id!");
            }
            em.getTransaction().commit();

        }

    }

    @Override
    public List<WorkoutProgram> getAll() {
        try(EntityManager em = emf.createEntityManager()){
           List<WorkoutProgram> allWorkoutPrograms = em.createQuery("SELECT w FROM WorkoutProgram w", WorkoutProgram.class)
                    .getResultList();
           return allWorkoutPrograms;
        }
    }

    @Override
    public List<WorkoutProgram> getByTrainingDaysPerWeek(int trainingDays) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT w FROM WorkoutProgram w WHERE w.trainingDaysPerWeek = :trainingDays", WorkoutProgram.class)
                    .setParameter("trainingDays", trainingDays)
                    .getResultList();
        }
    }
}
