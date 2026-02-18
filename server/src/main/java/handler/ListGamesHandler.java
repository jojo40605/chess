//TODO add comments for quality check
package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import result.ErrorResult;
import result.ListGamesResult;
import service.GameService;

public class ListGamesHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();

    public ListGamesHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");

            var games = gameService.listGames(authToken);

            ctx.status(200);
            ctx.json(new ListGamesResult(games));

        } catch (Exception e) {
            ctx.status(401);
            ctx.json(new ErrorResult(e.getMessage()));
        }
    }
}