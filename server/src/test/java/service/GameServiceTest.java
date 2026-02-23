package service;

import dataaccess.MemoryDataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    private GameService gameService;
    private MemoryDataAccess dataAccess;
    private String validToken;

    @BeforeEach
    void setup() throws DataAccessException {
        dataAccess = new MemoryDataAccess();
        gameService = new GameService(dataAccess);

        // Setup a default user/auth for testing games
        validToken = "valid-auth-token";
        dataAccess.createAuth(new AuthData(validToken, "player1"));
    }

    // --- Create Game Tests ---
    @Test
    void createGameSuccess() throws DataAccessException {
        int id = gameService.createGame(validToken, "MyGame");
        assertTrue(id > 0);
        assertNotNull(dataAccess.getGame(id));
    }

    @Test
    void createGameUnauthorized() {
        assertThrows(DataAccessException.class, () ->
                gameService.createGame("bad-token", "MyGame")
        );
    }

    // --- List Games Tests ---
    @Test
    void listGamesSuccess() throws DataAccessException {
        gameService.createGame(validToken, "Game1");
        Collection<GameData> games = gameService.listGames(validToken);
        assertEquals(1, games.size());
    }

    @Test
    void listGamesUnauthorized() {
        assertThrows(DataAccessException.class, () ->
                gameService.listGames("bad-token")
        );
    }

    // --- Join Game Tests ---
    @Test
    void joinGameSuccess() throws DataAccessException {
        int id = gameService.createGame(validToken, "JoinMe");
        assertDoesNotThrow(() ->
                gameService.joinGame(validToken, id, "WHITE")
        );
        assertEquals("player1", dataAccess.getGame(id).whiteUsername());
    }

    @Test
    void joinGameAlreadyTaken() throws DataAccessException {
        int id = gameService.createGame(validToken, "FullGame");
        gameService.joinGame(validToken, id, "WHITE");

        // Negative: Try to join as WHITE again
        assertThrows(DataAccessException.class, () ->
                gameService.joinGame(validToken, id, "WHITE")
        );
    }

}