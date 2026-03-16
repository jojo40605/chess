package client;

import org.junit.jupiter.api.*;
import server.Server;
import model.AuthData;
import result.ListGamesResult;
import result.CreateGameResult;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clear() {
        try {
            // This assumes you added a clear method to ServerFacade
            // that hits your DELETE /db endpoint
            facade.clear();
        } catch (Exception e) {
            System.out.println("Could not clear database: " + e.getMessage());
        }
    }

    // ===================== REGISTRATION TESTS =====================

    @Test
    @DisplayName("Positive Register")
    void registerSuccess() throws Exception {
        var authData = facade.register("player1", "password", "p1@email.com");
        assertNotNull(authData.authToken());
        assertEquals("player1", authData.username());
    }

    @Test
    @DisplayName("Negative Register - Duplicate")
    void registerFail() throws Exception {
        facade.register("unique", "pass", "u@u.com");
        // Expecting exception due to 403 Forbidden or 409 Conflict
        assertThrows(Exception.class, () -> facade.register("unique", "pass2", "u2@u.com"));
    }

    // ===================== LOGIN TESTS =====================

    @Test
    @DisplayName("Positive Login")
    void loginSuccess() throws Exception {
        facade.register("loginUser", "loginPass", "l@l.com");
        var authData = facade.login("loginUser", "loginPass");
        assertNotNull(authData.authToken());
    }

    @Test
    @DisplayName("Negative Login - Wrong Pass")
    void loginFail() throws Exception {
        facade.register("user", "pass", "u@u.com");
        assertThrows(Exception.class, () -> facade.login("user", "wrong_pass"));
    }

    // ===================== GAME TESTS =====================

    @Test
    @DisplayName("Positive Create Game")
    void createGameSuccess() throws Exception {
        var auth = facade.register("creator", "pass", "c@c.com");
        CreateGameResult result = facade.createGame(auth.authToken(), "Test Game");
        assertNotNull(result.gameID());
    }

    @Test
    @DisplayName("Negative Create Game - Bad Auth")
    void createGameFail() {
        assertThrows(Exception.class, () -> facade.createGame("invalid-token", "Fail Game"));
    }

    @Test
    @DisplayName("Positive List Games")
    void listGamesSuccess() throws Exception {
        var auth = facade.register("lister", "pass", "l@l.com");
        facade.createGame(auth.authToken(), "Game 1");
        facade.createGame(auth.authToken(), "Game 2");

        ListGamesResult result = facade.listGames(auth.authToken());
        assertEquals(2, result.games().size());
    }

    @Test
    @DisplayName("Negative List Games - No Auth")
    void listGamesFail() {
        assertThrows(Exception.class, () -> facade.listGames(null));
    }

    @Test
    @DisplayName("Positive Join Game")
    void joinGameSuccess() throws Exception {
        var auth = facade.register("joiner", "pass", "j@j.com");
        CreateGameResult game = facade.createGame(auth.authToken(), "Joinable Game");

        assertDoesNotThrow(() -> facade.joinGame(auth.authToken(), "WHITE", game.gameID()));
    }

    @Test
    @DisplayName("Negative Join Game - Bad ID")
    void joinGameFail() throws Exception {
        var auth = facade.register("failJoiner", "pass", "fj@j.com");
        assertThrows(Exception.class, () -> facade.joinGame(auth.authToken(), "BLACK", 999999));
    }

    // ===================== LOGOUT TESTS =====================

    @Test
    @DisplayName("Positive Logout")
    void logoutSuccess() throws Exception {
        var auth = facade.register("logoutUser", "pass", "lo@l.com");
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    @DisplayName("Negative Logout - Invalid Token")
    void logoutFail() {
        assertThrows(Exception.class, () -> facade.logout("bad-token"));
    }
}