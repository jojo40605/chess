//TODO add comments for quality
package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import request.JoinGameRequest;
import result.ErrorResult;
import service.GameService;

public class JoinGameHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();

    public JoinGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handle(Context ctx) {
        try {
            JoinGameRequest request = parseRequest(ctx);
            String authToken = getAuthToken(ctx);

            // Delegate to service for business logic
            gameService.joinGame(authToken, request.gameID(), request.playerColor());

            // Return success response
            ctx.status(200);
            ctx.result("{}");

        } catch (Exception e) {
            handleError(ctx, e);
        }
    }

    // ------------------- Helper Methods -------------------

    /** Extracts the Authorization header from the context */
    private String getAuthToken(Context ctx) {
        return ctx.header("Authorization");
    }

    /** Parses the request body into a JoinGameRequest object */
    private JoinGameRequest parseRequest(Context ctx) {
        return gson.fromJson(ctx.body(), JoinGameRequest.class);
    }

    /** Handles exceptions and sets appropriate HTTP status codes */
    private void handleError(Context ctx, Exception e) {
        String message = normalizeErrorMessage(e.getMessage());
        ctx.status(determineStatusCode(message));
        ctx.json(new ErrorResult(message));
    }

    /** Prepends "Error: " if not already present */
    private String normalizeErrorMessage(String message) {
        if (message == null) return "Error: Unknown error";
        return message.toLowerCase().contains("error") ? message : "Error: " + message;
    }

    /** Maps error messages to HTTP status codes */
    private int determineStatusCode(String message) {
        if (message.toLowerCase().contains("unauthorized")) return 401;
        if (message.toLowerCase().contains("already")) return 403;
        return 400; // default for other errors
    }
}