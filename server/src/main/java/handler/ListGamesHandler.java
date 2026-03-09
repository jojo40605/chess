package handler;

import dataaccess.DataAccessException;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import result.ErrorResult;
import result.ListGamesResult;
import service.GameService;

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
        }

        catch (DataAccessException e) {
            ctx.status(500);
            ctx.json(new ErrorResult("Error: " + e.getMessage()));
        }

        catch (Exception e) {
            HandlerUtils.handleError(ctx, e);
        }
    }
}