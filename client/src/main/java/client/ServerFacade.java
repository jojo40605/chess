package client;

import com.google.gson.Gson;
import model.AuthData;
import request.*;
import result.*;
import java.io.*;
import java.net.*;

public class ServerFacade {

    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    // ===================== PRE-LOGIN =====================

    public RegisterResult register(String username, String password, String email) throws Exception {
        var path = "/user";
        var request = new RegisterRequest(username, password, email);
        return this.makeRequest("POST", path, request, RegisterResult.class, null);
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

            // DELETE requests in some Java versions won't allow a body unless specified
            http.setDoOutput(!method.equals("GET"));

            writeHeader(authToken, http);
            writeBody(request, http);
            http.connect();

            // This method now extracts the specific "Error: ..." string from the server
            throwIfNotProcessable(http);

            return readBody(http, responseClass);
        } catch (IOException e) {
            throw new Exception("Error: Cannot connect to server. Ensure it is running.");
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
            // Read from the ErrorStream to get the ErrorResult JSON
            try (InputStream respBody = http.getErrorStream()) {
                if (respBody != null) {
                    InputStreamReader reader = new InputStreamReader(respBody);
                    ErrorResult error = gson.fromJson(reader, ErrorResult.class);
                    if (error != null && error.message() != null) {
                        // Throw the server's clean message (e.g., "Error: unauthorized")
                        throw new Exception(error.message());
                    }
                }
            }
            // Fallback if the error stream is empty
            throw new Exception("Error: " + status);
        }
    }

    private <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT && responseClass != null) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                response = gson.fromJson(reader, responseClass);
            }
        }
        return response;
    }
}