package app.testutils;

import app.entities.Exercise;
import app.entities.ExperienceLevel;
import app.entities.MuscleGroup;
import app.entities.TrainingGoal;
import app.entities.User;
import app.entities.WorkoutProgram;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.ArrayList;
import java.util.List;

public final class TestData {

    private TestData() {}

    public record Seed(User beginner, User advanced, Exercise benchPress, Exercise squat, WorkoutProgram fullBody, WorkoutProgram upperBody) {

    }

    public static Seed populate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM User").executeUpdate();
            for (WorkoutProgram program : em.createQuery("SELECT w FROM WorkoutProgram w", WorkoutProgram.class).getResultList()) {
                program.getExercises().clear();
            }
            em.flush();
            em.clear();
            em.createQuery("DELETE FROM WorkoutProgram").executeUpdate();
            em.createQuery("DELETE FROM Exercise").executeUpdate();

            Exercise benchPress = new Exercise("Bench Press", MuscleGroup.CHEST, 3, 10);
            Exercise squat = new Exercise("Squat", MuscleGroup.LEGS, 4, 8);
            em.persist(benchPress);
            em.persist(squat);

            WorkoutProgram fullBody = new WorkoutProgram("Full Body", "Beginner program", 3);
            fullBody.setExercises(new ArrayList<>(List.of(benchPress, squat)));
            WorkoutProgram upperBody = new WorkoutProgram("Upper Body", "Advanced program", 4);
            upperBody.setExercises(new ArrayList<>(List.of(benchPress)));
            em.persist(fullBody);
            em.persist(upperBody);

            User beginner = new User("Anna", "anna@test.dk", 25, 170, 65, ExperienceLevel.BEGINNER, 3, TrainingGoal.MUSCLE_GAIN);
            beginner.setWorkoutProgram(fullBody);
            User advanced = new User("Omar", "omar@test.dk", 30, 185, 85, ExperienceLevel.ADVANCED, 4, TrainingGoal.STRENGTH);
            em.persist(beginner);
            em.persist(advanced);
            em.getTransaction().commit();
            return new Seed(beginner, advanced, benchPress, squat, fullBody, upperBody);
        }
    }
}
