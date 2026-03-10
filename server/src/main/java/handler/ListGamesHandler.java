package handler;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import result.ListGamesResult;
import result.ErrorResult;
import service.GameService;
import service.UnauthorizedException;
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
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(new ErrorResult("Error: " + e.getMessage()));
        }
    }
}