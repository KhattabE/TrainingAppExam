
package app.services;

import app.dao.UserDAO;
import app.dao.UserDAOImpl;
import app.dto.GeneratedExerciseDTO;
import app.dto.GeneratedWorkoutProgramDTO;
import app.entities.Exercise;
import app.entities.User;
import app.entities.WorkoutProgram;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WorkoutProgramService {

    private final EntityManagerFactory emf;
    private final UserDAO userDAO;
    private final AiCoachService aiCoachService;

    public WorkoutProgramService(EntityManagerFactory emf, AiCoachService aiCoachService) {
        this.emf = emf;
        this.userDAO = new UserDAOImpl(emf);
        this.aiCoachService = aiCoachService;
    }

    public WorkoutProgram generateForUser(int userId) throws IOException, InterruptedException {
        User user = userDAO.getById(userId);

        if (user == null) {
            throw new ApiException(404, "User not found");
        }

        GeneratedWorkoutProgramDTO generatedProgram = aiCoachService.generateWorkoutProgram(user);
        validateProgram(generatedProgram);

        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            try {
                User managedUser = em.find(User.class, userId);

                if (managedUser == null) {
                    throw new ApiException(404, "User not found");
                }

                List<Exercise> exercises = new ArrayList<>();

                for (GeneratedExerciseDTO generatedExercise : generatedProgram.exercises()) {
                    Exercise exercise = new Exercise(generatedExercise.name(), generatedExercise.muscleGroup(), generatedExercise.sets(), generatedExercise.reps());

                    em.persist(exercise);
                    exercises.add(exercise);
                }

                WorkoutProgram program = new WorkoutProgram(generatedProgram.name(), generatedProgram.description(), managedUser.getTrainingDaysPerWeek());

                program.setExercises(exercises);
                em.persist(program);

                managedUser.setWorkoutProgram(program);

                em.getTransaction().commit();
                return program;

            } catch (RuntimeException exception) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw exception;
            }
        }
    }

    public WorkoutProgram getForUser(int userId) {
        try (EntityManager em = emf.createEntityManager()) {

            User user = em.find(User.class, userId);

            if (user == null) {
                throw new ApiException(404, "User not found");
            }

            WorkoutProgram program = user.getWorkoutProgram();

            if (program == null) {
                throw new ApiException(404, "User has no workout program");
            }

            // Load exercises before closing EntityManager
            program.getExercises().size();

            return program;
        }
    }

    private void validateProgram(GeneratedWorkoutProgramDTO program) {
        if (program == null || isBlank(program.name()) || isBlank(program.description()) || program.exercises() == null || program.exercises().isEmpty()) {

            throw new ApiException(502, "AI generated an invalid workout program");
        }

        for (GeneratedExerciseDTO exercise : program.exercises()) {
            if (exercise == null || isBlank(exercise.name()) || exercise.muscleGroup() == null || exercise.sets() <= 0 || exercise.reps() <= 0) {

                throw new ApiException(502, "AI generated an invalid exercise");
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
