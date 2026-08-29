package app.dao;


import app.entities.WorkoutProgram;

import java.util.List;

public interface WorkoutProgramDAO {

    WorkoutProgram create(WorkoutProgram  workoutProgram);

    WorkoutProgram  getById(int id);

    WorkoutProgram  update(WorkoutProgram  workoutProgram);

    void delete(int id);

    List<WorkoutProgram> getAll();

    List<WorkoutProgram> getByTrainingDaysPerWeek(int trainingDays);


}
