package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class UserService {

    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    // ---------------- REGISTER ----------------

    public AuthData register(String username, String password, String email)
            throws DataAccessException {

        if (username == null || password == null || email == null) {
            throw new DataAccessException("Error: bad request");
        }

        if (dataAccess.getUser(username) != null) {
            throw new DataAccessException("Error: already taken");
        }

        UserData user = new UserData(username, password, email);
        dataAccess.createUser(user);

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        dataAccess.createAuth(authData);

        return authData;
    }

    // ---------------- LOGIN ----------------

    public AuthData login(String username, String password)
            throws DataAccessException {

        UserData user = dataAccess.getUser(username);

        if (user == null || !user.password().equals(password)) {
            throw new DataAccessException("Error: unauthorized");
        }

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        dataAccess.createAuth(authData);

        return authData;
    }

    // ---------------- LOGOUT ----------------

    public void logout(String authToken)
            throws DataAccessException {

        AuthData auth = dataAccess.getAuth(authToken);

        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        dataAccess.deleteAuth(authToken);
    }
}
