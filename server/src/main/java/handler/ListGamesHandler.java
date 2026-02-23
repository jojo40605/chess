package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import result.ErrorResult;
import result.ListGamesResult;
import service.GameService;

/**
 * Handles HTTP requests to retrieve a list of all existing chess games.
 * Requires a valid authorization token in the request header.
 */
public class ListGamesHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();

    public ListGamesHandler(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Processes the list games request.
     * * @param ctx the Javalin context representing the HTTP request/response
     */
    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");

            // Service layer handles the business logic and auth validation
            var games = gameService.listGames(authToken);

            ctx.status(HttpStatus.OK);
            ctx.json(new ListGamesResult(games));

        } catch (Exception e) {
            handleError(ctx, e);
        }
    }

    /**
     * Maps errors to the appropriate HTTP status.
     * Specifically looks for unauthorized access attempts.
     */
    private void handleError(Context ctx, Exception e) {
        String message = e.getMessage();

        if (message != null && message.toLowerCase().contains("unauthorized")) {
            ctx.status(HttpStatus.UNAUTHORIZED);
        } else {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ctx.json(new ErrorResult("Error: " + message));
    }
}