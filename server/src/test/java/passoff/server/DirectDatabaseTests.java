package passoff.server;

import chess.ChessGame;
import dataaccess.DatabaseDataAccess;
import dataaccess.DataAccessException;
import model.GameData;
import model.UserData;
import model.AuthData;
import org.junit.jupiter.api.*;

import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DirectDatabaseTests {

    private static DatabaseDataAccess db;

    private static final UserData TEST_USER = new UserData("TestUser", "Password123", "test@example.com");
    private static final String TEST_AUTH_TOKEN = "auth-token-123";

    @BeforeAll
    public static void setupClass() {
        db = new DatabaseDataAccess();
    }

    @BeforeEach
    public void clearDatabase() throws DataAccessException {
        db.clear();
    }

    @Test
    @DisplayName("User Creation and Retrieval")
    public void testUserPersistence() throws DataAccessException {
        db.createUser(TEST_USER);

        UserData retrieved = db.getUser(TEST_USER.username());
        Assertions.assertNotNull(retrieved, "User was not retrieved");
        Assertions.assertEquals(TEST_USER.username(), retrieved.username(), "Username mismatch");
        Assertions.assertEquals(TEST_USER.email(), retrieved.email(), "Email mismatch");
    }

    @Test
    @DisplayName("Auth Creation and Retrieval")
    public void testAuthPersistence() throws DataAccessException {
        db.createUser(TEST_USER);
        db.createAuth(new AuthData(TEST_AUTH_TOKEN, TEST_USER.username()));

        AuthData retrieved = db.getAuth(TEST_AUTH_TOKEN);
        Assertions.assertNotNull(retrieved, "Auth was not retrieved");
        Assertions.assertEquals(TEST_USER.username(), retrieved.username(), "Auth username mismatch");
    }

    @Test
    @DisplayName("Game Creation and Retrieval")
    public void testGamePersistence() throws DataAccessException {
        ChessGame game = new ChessGame(); // make sure ChessGame implements Serializable
        GameData gameData = new GameData(0, TEST_USER.username(), null, "Test Game", game);

        int gameID = db.createGame(gameData);
        Assertions.assertTrue(gameID > 0, "Game ID not returned correctly");

        GameData retrieved = db.getGame(gameID);
        Assertions.assertNotNull(retrieved, "Game was not retrieved");
        Assertions.assertEquals("Test Game", retrieved.gameName(), "Game name mismatch");
        Assertions.assertEquals(TEST_USER.username(), retrieved.whiteUsername(), "White username mismatch");
        Assertions.assertNotNull(retrieved.game(), "ChessGame object was null");
    }

    @Test
    @DisplayName("Game Update")
    public void testGameUpdate() throws DataAccessException {
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(0, TEST_USER.username(), null, "Original Game", game);

        int gameID = db.createGame(gameData);

        // Update the game
        gameData = new GameData(gameID, TEST_USER.username(), "Opponent", "Updated Game", game);
        db.updateGame(gameData);

        GameData retrieved = db.getGame(gameID);
        Assertions.assertEquals("Updated Game", retrieved.gameName(), "Game name was not updated");
        Assertions.assertEquals("Opponent", retrieved.blackUsername(), "Black username was not updated");
    }

    @Test
    @DisplayName("List Games")
    public void testListGames() throws DataAccessException {
        ChessGame game1 = new ChessGame();
        ChessGame game2 = new ChessGame();

        db.createGame(new GameData(0, TEST_USER.username(), null, "Game1", game1));
        db.createGame(new GameData(0, TEST_USER.username(), null, "Game2", game2));

        List<GameData> games = db.listGames();
        Assertions.assertEquals(2, games.size(), "Number of games in database mismatch");
    }
}