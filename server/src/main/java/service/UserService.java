package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import java.util.UUID;

/**
 * Service class for user-related operations including registration, login, and logout.
 * Interfaces with the DataAccess layer to persist and retrieve user and session information.
 */
public class UserService {

    private final DataAccess dataAccess;

    /**
     * Constructs a UserService with a specified data access object.
     *
     * @param dataAccess the data access object for user and auth operations
     */
    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    /**
     * Registers a new user and generates an initial authorization token.
     *
     * @param username requested username
     * @param password user's password
     * @param email    user's email address
     * @return AuthData containing the new token and username
     * @throws DataAccessException if fields are missing or the username is already taken
     */
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

        return generateAuth(username);
    }

    /**
     * Authenticates a user and generates a new authorization token.
     *
     * @param username user's username
     * @param password user's password
     * @return AuthData containing the new token and username
     * @throws DataAccessException if authentication fails
     */
    public AuthData login(String username, String password)
            throws DataAccessException {

        UserData user = dataAccess.getUser(username);

        if (user == null || !user.password().equals(password)) {
            throw new DataAccessException("Error: unauthorized");
        }

        return generateAuth(username);
    }

    /**
     * Invalidates an existing authorization token.
     *
     * @param authToken the token to remove
     * @throws DataAccessException if the token does not exist
     */
    public void logout(String authToken)
            throws DataAccessException {

        AuthData auth = dataAccess.getAuth(authToken);

        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        dataAccess.deleteAuth(authToken);
    }

    /**
     * Helper method to generate and persist a new AuthData object.
     */
    private AuthData generateAuth(String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        dataAccess.createAuth(authData);
        return authData;
    }
}