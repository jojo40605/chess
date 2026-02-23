package integration;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import server.Server;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTests {

    private static Server server;
    private static int port;
    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();
    private final String baseUrl = "http://localhost:";

    @BeforeAll
    static void startServer() {
        server = new Server();
        port = server.run(0); // 0 finds an available port
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + port + "/db"))
                .DELETE()
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    @DisplayName("Register - Success")
    void registerSuccess() throws Exception {
        var body = Map.of("username", "player1", "password", "pass", "email", "p1@byu.edu");

        HttpResponse<String> response = sendRequest("/user", "POST", body, null);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("authToken"));
    }

    @Test
    @DisplayName("Create Game - Success")
    void createGameSuccess() throws Exception {
        // First, register to get a token
        String token = getToken("userA", "passA", "a@a.com");

        var gameBody = Map.of("gameName", "Test Chess Game");
        HttpResponse<String> response = sendRequest("/game", "POST", gameBody, token);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("gameID"));
    }

    @Test
    @DisplayName("Join Game - Failure (Empty ID)")
    void joinGameFailure() throws Exception {
        String token = getToken("userB", "passB", "b@b.com");

        var joinBody = Map.of("playerColor", "WHITE", "gameID", 0);
        HttpResponse<String> response = sendRequest("/game", "PUT", joinBody, token);

        // Expect 400 or 401 depending on your service logic for invalid IDs
        assertNotEquals(200, response.statusCode());
    }

    @Test
    @DisplayName("List Games - Unauthorized")
    void listGamesUnauthorized() throws Exception {
        HttpResponse<String> response = sendRequest("/game", "GET", null, "invalid-token");
        assertEquals(401, response.statusCode());
    }

    // --- Helper Methods for Cleaner Tests ---

    private String getToken(String u, String p, String e) throws Exception {
        var body = Map.of("username", u, "password", p, "email", e);
        HttpResponse<String> res = sendRequest("/user", "POST", body, null);
        Map<String, Object> map = gson.fromJson(res.body(), Map.class);
        return (String) map.get("authToken");
    }

    private HttpResponse<String> sendRequest(String path, String method, Object body, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + port + path))
                .header("Content-Type", "application/json");

        if (token != null) {builder.header("Authorization", token);}

        HttpRequest.BodyPublisher bodyPublisher = (body == null)
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(gson.toJson(body));

        builder.method(method, bodyPublisher);
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}