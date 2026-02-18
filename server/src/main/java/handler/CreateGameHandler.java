//TODO add comments for quality
package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import request.CreateGameRequest;
import result.CreateGameResult;
import result.ErrorResult;
import service.GameService;

public class CreateGameHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();

    public CreateGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");

            CreateGameRequest request =
                    gson.fromJson(ctx.body(), CreateGameRequest.class);

            int gameID =
                    gameService.createGame(authToken, request.gameName());

            ctx.status(200);
            ctx.json(new CreateGameResult(gameID));

        } catch (Exception e) {
            setStatus(ctx, e.getMessage());
            ctx.json(new ErrorResult(e.getMessage()));
        }
    }

    private void setStatus(Context ctx, String msg) {
        if (msg.contains("unauthorized")) ctx.status(401);
        else ctx.status(400);
    }
}