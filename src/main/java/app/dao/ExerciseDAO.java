package app.dao;

import app.entities.Exercise;
import app.entities.MuscleGroup;

import java.util.List;

public interface ExerciseDAO {

    Exercise create(Exercise exercise);

    Exercise getById(int id);

    Exercise update(Exercise exercise);

    void delete(int id);

    List<Exercise> getAll();

    List<Exercise> getByMuscleGroup(MuscleGroup muscleGroup);
}