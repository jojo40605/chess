package client;

import com.google.gson.Gson;
import model.AuthData;
import request.*;
import result.*;
import java.io.*;
import java.net.*;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    // ===================== PRE-LOGIN =====================

    public AuthData register(String username, String password, String email) throws Exception {
        var path = "/user";
        var request = new RegisterRequest(username, password, email);
        return this.makeRequest("POST", path, request, AuthData.class, null);
    }

    public AuthData login(String username, String password) throws Exception {
        var path = "/session";
        var request = new LoginRequest(username, password);
        return this.makeRequest("POST", path, request, AuthData.class, null);
    }

    // ===================== POST-LOGIN =====================

    public void logout(String authToken) throws Exception {
        var path = "/session";
        this.makeRequest("DELETE", path, null, null, authToken);
    }

    public CreateGameResult createGame(String authToken, String gameName) throws Exception {
        var path = "/game";
        var request = new CreateGameRequest(gameName);
        return this.makeRequest("POST", path, request, CreateGameResult.class, authToken);
    }

    public ListGamesResult listGames(String authToken) throws Exception {
        var path = "/game";
        return this.makeRequest("GET", path, null, ListGamesResult.class, authToken);
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws Exception {
        var path = "/game";
        var request = new JoinGameRequest(gameID, playerColor);
        this.makeRequest("PUT", path, request, null, authToken);
    }

    public void clear() throws Exception {
        this.makeRequest("DELETE", "/db", null, null, null);
    }

    // ===================== CORE HTTP LOGIC =====================

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws Exception {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(!method.equals("GET")); // No body for GET requests

            writeHeader(authToken, http);
            writeBody(request, http);
            http.connect();
            throwIfNotProcessable(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            // This catches connection issues OR the custom error from throwIfNotProcessable
            throw new Exception(ex.getMessage());
        }
    }

    private static void writeHeader(String authToken, HttpURLConnection http) {
        if (authToken != null) {
            http.addRequestProperty("Authorization", authToken);
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotProcessable(HttpURLConnection http) throws Exception {
        var status = http.getResponseCode();
        if (status < 200 || status >= 300) {
            // Logic to read error message from server if applicable
            throw new Exception("Error: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT && responseClass != null) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                response = new Gson().fromJson(reader, responseClass);
            }
        }
        return response;
    }
}