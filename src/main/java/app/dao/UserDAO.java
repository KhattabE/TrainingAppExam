package app.dao;

import app.entities.ExperienceLevel;
import app.entities.User;

import java.util.List;

public interface UserDAO {

    User create(User user);

    User getById(int id);

    User update(User user);

    void delete(int id);

    List<User> getAll();

    List<User> getByExperienceLevel(ExperienceLevel experienceLevel);

    List<User> getByWorkoutProgramName(String programName);
}