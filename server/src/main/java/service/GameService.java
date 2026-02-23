package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import java.util.List;

/**
 * Service class for handling chess game business logic.
 * Manages game creation, retrieval, and player participation.
 */
public class GameService {

    private final DataAccess dataAccess;

    /**
     * Constructs a GameService with the specified data access object.
     *
     * @param dataAccess the data access object for database operations
     */
    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    /**
     * Creates a new game with a unique ID and the provided name.
     *
     * @param authToken valid authorization token
     * @param gameName name of the new game
     * @return the generated game ID
     * @throws DataAccessException if unauthorized or request is invalid
     */
    public int createGame(String authToken, String gameName) throws DataAccessException {
        validateAuth(authToken);

        if (gameName == null) {
            throw new DataAccessException("Error: bad request");
        }

        // Logic relies on specific MemoryDataAccess implementation for ID generation
        int gameID = ((dataaccess.MemoryDataAccess) dataAccess).generateGameID();

        GameData game = new GameData(
                gameID,
                null,
                null,
                gameName,
                new ChessGame()
        );

        dataAccess.createGame(game);
        return gameID;
    }

    /**
     * Retrieves a list of all current games.
     *
     * @param authToken valid authorization token
     * @return a list containing all game data
     * @throws DataAccessException if unauthorized
     */
    public List<GameData> listGames(String authToken) throws DataAccessException {
        validateAuth(authToken);
        return dataAccess.listGames();
    }

    /**
     * Adds a user to an existing game in the specified color slot.
     *
     * @param authToken valid authorization token
     * @param gameID the ID of the game to join
     * @param playerColor the color the player wishes to play (WHITE or BLACK)
     * @throws DataAccessException if game is full, ID invalid, or unauthorized
     */
    public void joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        AuthData auth = validateAuth(authToken);
        GameData game = dataAccess.getGame(gameID);

        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        String white = game.whiteUsername();
        String black = game.blackUsername();

        if (playerColor == null) {
            throw new DataAccessException("Error: bad request");
        }

        if (playerColor.equalsIgnoreCase("WHITE")) {
            if (white != null) {
                throw new DataAccessException("Error: already taken");
            }
            game = new GameData(gameID, auth.username(), black, game.gameName(), game.game());
        } else if (playerColor.equalsIgnoreCase("BLACK")) {
            if (black != null) {
                throw new DataAccessException("Error: already taken");
            }
            game = new GameData(gameID, white, auth.username(), game.gameName(), game.game());
        } else {
            throw new DataAccessException("Error: bad request");
        }

        dataAccess.updateGame(game);
    }

    /**
     * Validates an authorization token against the database.
     *
     * @param authToken token to validate
     * @return the AuthData associated with the token
     * @throws DataAccessException if the token is invalid or missing
     */
    private AuthData validateAuth(String authToken) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);

        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        return auth;
    }
}