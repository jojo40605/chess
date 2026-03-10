package handler;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import result.ListGamesResult;
import service.GameService;
import dataaccess.DataAccessException;

public class ListGamesHandler {

    private final GameService gameService;

    public ListGamesHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");

            var games = gameService.listGames(authToken);

            ctx.status(HttpStatus.OK);
            ctx.json(new ListGamesResult(games));

        } catch (DataAccessException e) {
            HandlerUtils.handleError(ctx, e);
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("unauthorized")) {
                ctx.status(401);
            } else {
                ctx.status(400);
            }
            ctx.json(new result.ErrorResult("Error: " + e.getMessage()));
        }
    }
}