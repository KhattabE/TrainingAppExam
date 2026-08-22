package app.dao;

import app.entities.User;

public interface UserDAO {

    User create(User user);

    User getById(int id);

    User update(User user);

    void delete(int id);


}
