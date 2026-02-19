//TODO update for code quality check
package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

import java.util.List;

public class GameService {

    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    // ---------------- CREATE GAME ----------------

    public int createGame(String authToken, String gameName)
            throws DataAccessException {

        validateAuth(authToken);

        if (gameName == null) {
            throw new DataAccessException("Error: bad request");
        }

        int gameID =
                ((dataaccess.MemoryDataAccess) dataAccess).generateGameID();

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

    // ---------------- LIST GAMES ----------------

    public List<GameData> listGames(String authToken)
            throws DataAccessException {

        validateAuth(authToken);
        return dataAccess.listGames();
    }

    // ---------------- JOIN GAME ----------------

    public void joinGame(String authToken, int gameID, String playerColor)
            throws DataAccessException {

        AuthData auth = validateAuth(authToken);

        GameData game = dataAccess.getGame(gameID);

        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        String white = game.whiteUsername();
        String black = game.blackUsername();

        if (playerColor.equalsIgnoreCase("WHITE")) {

            if (white != null) {
                throw new DataAccessException("Error: already taken");
            }

            game = new GameData(
                    gameID,
                    auth.username(),
                    black,
                    game.gameName(),
                    game.game()
            );

        } else if (playerColor.equalsIgnoreCase("BLACK")) {

            if (black != null) {
                throw new DataAccessException("Error: already taken");
            }

            game = new GameData(
                    gameID,
                    white,
                    auth.username(),
                    game.gameName(),
                    game.game()
            );

        } else {
            throw new DataAccessException("Error: bad request");
        }

        dataAccess.updateGame(game);
    }

    private AuthData validateAuth(String authToken)
            throws DataAccessException {

        AuthData auth = dataAccess.getAuth(authToken);

        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        return auth;
    }
}
