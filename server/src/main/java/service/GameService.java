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
    public int createGame(String authToken, String gameName)
            throws BadRequestException, UnauthorizedException, DataAccessException {

        if (authToken == null || authToken.isBlank()) {
            throw new UnauthorizedException("unauthorized");
        }

        // validate auth token with DataAccess
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("unauthorized");
        }

        if (gameName == null || gameName.isBlank()) {
            throw new BadRequestException("bad request");
        }

        // Create a new game object; initially no players
        GameData game = new GameData(
                0,          // ID will be set by the database
                null,       // white player
                null,       // black player
                gameName,
                new ChessGame()
        );

        // Let DatabaseDataAccess return the generated ID
        int gameID = dataAccess.createGame(game);

        return gameID;
    }

    /**
     * Retrieves a list of all current games.
     *
     * @param authToken valid authorization token
     * @return a list containing all game data
     * @throws DataAccessException if unauthorized
     */
    public List<GameData> listGames(String authToken) throws DataAccessException, UnauthorizedException {
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
    public void joinGame(String authToken, int gameID, String playerColor)
            throws BadRequestException, UnauthorizedException, DataAccessException, ForbiddenException {

        // 1. Validate auth token using your existing getAuth method
        if (authToken == null || authToken.isBlank()) {
            throw new UnauthorizedException("unauthorized");
        }
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("unauthorized");
        }

        // 2. Get the game
        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            throw new BadRequestException("bad request");
        }

        // 3. OBSERVER LOGIC: Allow join if playerColor is null or empty
        if (playerColor == null || playerColor.isBlank()) {
            return; // Success! No database update needed for observers in this phase.
        }

        String white = game.whiteUsername();
        String black = game.blackUsername();
        String username = auth.username();

        // 4. PLAYER LOGIC: Handle White/Black slots with Rejoin support
        if (playerColor.equalsIgnoreCase("WHITE")) {
            // If slot is taken AND it's not taken by the current user, block it.
            if (white != null && !white.equals(username)) {
                throw new ForbiddenException("already taken");
            }
            game = new GameData(gameID, username, black, game.gameName(), game.game());
        }
        else if (playerColor.equalsIgnoreCase("BLACK")) {
            // If slot is taken AND it's not taken by the current user, block it.
            if (black != null && !black.equals(username)) {
                throw new ForbiddenException("already taken");
            }
            game = new GameData(gameID, white, username, game.gameName(), game.game());
        }
        else {
            throw new BadRequestException("bad request");
        }

        // 5. Update the game in the database
        dataAccess.updateGame(game);
    }

    /**
     * Validates an authorization token against the database.
     *
     * @param authToken token to validate
     * @return the AuthData associated with the token
     * @throws DataAccessException if the token is invalid or missing
     */
    private AuthData validateAuth(String authToken) throws UnauthorizedException, DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);

        if (auth == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        return auth;
    }
}