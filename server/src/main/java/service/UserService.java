package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {

    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    // ===================== REGISTER =====================
    public AuthData register(String username, String password, String email)
            throws DataAccessException, BadRequestException, ConflictException {

        if (username == null || username.isBlank() ||
                password == null || password.isBlank() ||
                email == null || email.isBlank()) {
            throw new BadRequestException("bad request");
        }

        if (dataAccess.getUser(username) != null) {
            throw new ConflictException("already taken");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        UserData user = new UserData(username, hashedPassword, email);
        dataAccess.createUser(user);

        return generateAuth(username);
    }

    // ===================== LOGIN =====================
    public AuthData login(String username, String password)
            throws DataAccessException, UnauthorizedException {

        UserData user = dataAccess.getUser(username);

        if (user == null || !BCrypt.checkpw(password, user.password())) {
            throw new UnauthorizedException("unauthorized");
        }

        return generateAuth(username);
    }

    // ===================== LOGOUT =====================
    public void logout(String authToken)
            throws DataAccessException, UnauthorizedException {

        AuthData auth = dataAccess.getAuth(authToken);

        if (auth == null) {
            throw new UnauthorizedException("unauthorized");
        }

        dataAccess.deleteAuth(authToken);
    }

    // ===================== HELPER =====================
    private AuthData generateAuth(String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        dataAccess.createAuth(authData);
        return authData;
    }
}