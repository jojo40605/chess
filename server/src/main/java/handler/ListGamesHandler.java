package handler;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import result.ListGamesResult;
import service.GameService;

/**
 * Handles HTTP requests to retrieve a list of all existing chess games.
 * Requires a valid authorization token in the request header.
 */
public class ListGamesHandler {

    private final GameService gameService;

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
            HandlerUtils.handleError(ctx, e);
        }
    }
}