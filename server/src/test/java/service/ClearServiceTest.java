package service;

import dataaccess.MemoryDataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import model.GameData;
import chess.ChessGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {
    private ClearService clearService;
    private MemoryDataAccess dataAccess;

    @BeforeEach
    void setup() {
        dataAccess = new MemoryDataAccess();
        clearService = new ClearService(dataAccess);
    }

    @Test
    void clearSuccess() throws DataAccessException {
        // 1. Manually populate dataAccess (bypasses Auth checks in GameService)
        dataAccess.createUser(new UserData("Player1", "pass", "p1@email.com"));
        dataAccess.createAuth(new AuthData("some-token", "Player1"));
        dataAccess.createGame(new GameData(1, null, null, "TestGame", new ChessGame()));

        // 2. Run the clear
        clearService.clear();

        // 3. Positive Test: Verify the data is actually gone
        assertNull(dataAccess.getUser("Player1"), "User should be deleted");
        assertNull(dataAccess.getAuth("some-token"), "Auth token should be deleted");
        assertTrue(dataAccess.listGames().isEmpty(), "Games list should be empty");
    }

    @Test
    void clearEmpty() {
        // 4. Negative/Edge Case: Clear an already empty database
        assertDoesNotThrow(() -> clearService.clear(), "Clearing an empty DB shouldn't fail");
    }
}