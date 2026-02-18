package dataaccess;

import model.UserData;
import model.AuthData;
import model.GameData;

import java.util.List;

public interface DataAccess {

    // Clear
    void clear() throws DataAccessException;

    // User methods
    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;

    // Auth methods
    void createAuth(AuthData auth) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;

    // Game methods
    void createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
}
