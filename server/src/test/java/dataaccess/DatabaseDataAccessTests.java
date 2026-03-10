package dataaccess;

import model.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseDataAccessTests {

    private DatabaseDataAccess dataAccess;
    private UserData testUser;
    private AuthData testAuth;

    @BeforeEach
    public void setUp() throws DataAccessException {
        dataAccess = new DatabaseDataAccess();
        // Clear before every test to ensure isolation
        dataAccess.clear();

        testUser = new UserData("player1", "password", "p1@chess.com");
        testAuth = new AuthData("valid-token-123", "player1");
    }

    // ===================== CLEAR TESTS =====================

    @Test
    @DisplayName("Clear Positive: Database is emptied")
    public void clearPositive() throws DataAccessException {
        dataAccess.createUser(testUser);
        dataAccess.createAuth(testAuth);

        dataAccess.clear();

        assertNull(dataAccess.getUser(testUser.username()));
        assertNull(dataAccess.getAuth(testAuth.authToken()));
    }

    // ===================== USER TESTS =====================

    @Test
    @DisplayName("Create User Positive: Successfully adds user")
    public void createUserPositive() throws DataAccessException {
        dataAccess.createUser(testUser);
        UserData retrieved = dataAccess.getUser(testUser.username());
        assertEquals(testUser, retrieved, "Retrieved user should match the one created");
    }

    @Test
    @DisplayName("Create User Negative: Duplicate username fails")
    public void createUserNegative() throws DataAccessException {
        dataAccess.createUser(testUser);
        // Attempting to create the same user again should throw a DataAccessException
        assertThrows(DataAccessException.class, () -> dataAccess.createUser(testUser));
    }

    @Test
    @DisplayName("Get User Positive: Retrieve existing user")
    public void getUserPositive() throws DataAccessException {
        dataAccess.createUser(testUser);
        assertNotNull(dataAccess.getUser("player1"));
    }

    @Test
    @DisplayName("Get User Negative: Return null for non-existent user")
    public void getUserNegative() throws DataAccessException {
        assertNull(dataAccess.getUser("fakeUser"));
    }

    // ===================== AUTH TESTS =====================

    @Test
    @DisplayName("Create Auth Positive: Successfully adds token")
    public void createAuthPositive() throws DataAccessException {
        dataAccess.createUser(testUser);
        dataAccess.createAuth(testAuth);
        assertEquals(testAuth, dataAccess.getAuth(testAuth.authToken()));
    }

    @Test
    @DisplayName("Create Auth Negative: Duplicate token fails")
    public void createAuthNegative() throws DataAccessException {
        dataAccess.createUser(testUser);
        dataAccess.createAuth(testAuth);
        assertThrows(DataAccessException.class, () -> dataAccess.createAuth(testAuth));
    }

    @Test
    @DisplayName("Delete Auth Positive: Token is removed")
    public void deleteAuthPositive() throws DataAccessException {
        dataAccess.createUser(testUser);
        dataAccess.createAuth(testAuth);
        dataAccess.deleteAuth(testAuth.authToken());
        assertNull(dataAccess.getAuth(testAuth.authToken()));
    }

    @Test
    @DisplayName("Delete Auth Negative: Deleting non-existent token does nothing")
    public void deleteAuthNegative() throws DataAccessException {
        // Should not throw an exception even if the token isn't there
        assertDoesNotThrow(() -> dataAccess.deleteAuth("non-existent-token"));
    }

    // ===================== GAME TESTS =====================

    @Test
    @DisplayName("Create Game Positive: Returns valid ID")
    public void createGamePositive() throws DataAccessException {
        GameData newGame = new GameData(0, null, null, "Test Match", null);
        int id = dataAccess.createGame(newGame);
        assertTrue(id > 0, "Game ID should be a positive generated integer");
    }

    @Test
    @DisplayName("Create Game Negative: Invalid username fails")
    public void createGameNegative() {
        // This will fail because "nonExistentUser" isn't in the users table
        GameData badGame = new GameData(0, "nonExistentUser", null, "Test Game", null);

        assertThrows(DataAccessException.class, () -> dataAccess.createGame(badGame));
    }

    @Test
    @DisplayName("List Games Positive: Returns all games")
    public void listGamesPositive() throws DataAccessException {
        // 1. Create the users that will be referenced in the games
        dataAccess.createUser(new UserData("w", "pass", "w@email.com"));
        dataAccess.createUser(new UserData("b", "pass", "b@email.com"));
        dataAccess.createUser(new UserData("w2", "pass", "w2@email.com"));
        dataAccess.createUser(new UserData("b2", "pass", "b2@email.com"));

        // 2. Create the games
        dataAccess.createGame(new GameData(0, "w", "b", "G1", null));
        dataAccess.createGame(new GameData(0, "w2", "b2", "G2", null));

        List<GameData> games = dataAccess.listGames();
        assertEquals(2, games.size());
    }

    @Test
    @DisplayName("List Games Negative: Empty list when no games exist")
    public void listGamesNegative() throws DataAccessException {
        List<GameData> games = dataAccess.listGames();
        assertNotNull(games);
        assertEquals(0, games.size());
    }

    @Test
    @DisplayName("Update Game Positive: Successfully changes game state")
    public void updateGamePositive() throws DataAccessException {
        // 1. Create the users first so the Foreign Key constraints are happy
        dataAccess.createUser(new UserData("whitePlayer", "pass", "w@email.com"));
        dataAccess.createUser(new UserData("blackPlayer", "pass", "b@email.com"));

        // 2. Create the initial game (using null or valid users)
        int id = dataAccess.createGame(new GameData(0, null, null, "Old Name", null));

        // 3. Now perform the update with those existing usernames
        GameData updated = new GameData(id, "whitePlayer", "blackPlayer", "New Name", null);
        dataAccess.updateGame(updated);

        // 4. Verify
        GameData retrieved = dataAccess.getGame(id);
        assertEquals("whitePlayer", retrieved.whiteUsername());
        assertEquals("blackPlayer", retrieved.blackUsername());
        assertEquals("New Name", retrieved.gameName());
    }

    @Test
    @DisplayName("Update Game Negative: Update non-existent ID affects nothing")
    public void updateGameNegative() throws DataAccessException {
        // Try to update game ID 999 which doesn't exist
        GameData phantomGame = new GameData(999, "w", "b", "Ghost", null);
        assertDoesNotThrow(() -> dataAccess.updateGame(phantomGame));

        // Ensure database is still empty/unaffected
        assertEquals(0, dataAccess.listGames().size());
    }

    @Test
    @DisplayName("Get Game Positive: Retrieve existing game")
    public void getGamePositive() throws DataAccessException {
        int id = dataAccess.createGame(new GameData(0, null, null, "Find Me", null));
        GameData retrieved = dataAccess.getGame(id);
        assertNotNull(retrieved);
        assertEquals("Find Me", retrieved.gameName());
    }

    @Test
    @DisplayName("Get Game Negative: Return null for non-existent ID")
    public void getGameNegative() throws DataAccessException {
        // ID 9999 should not exist in a fresh database
        GameData retrieved = dataAccess.getGame(9999);
        assertNull(retrieved, "Should return null when game ID is not found");
    }
}