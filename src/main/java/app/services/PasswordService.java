package app.services;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordService {

    public String hashPassword(String password) {
        String salted = BCrypt.gensalt();
        return BCrypt.hashpw(password, salted);
    }

    public boolean verifyPassword(String password, String hashedPassword) {
          return BCrypt.checkpw(password, hashedPassword);
    }



}
