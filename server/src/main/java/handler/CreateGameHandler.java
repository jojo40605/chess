package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import request.CreateGameRequest;
import result.CreateGameResult;
import result.ErrorResult;
import service.GameService;
import dataaccess.DataAccessException;

public class CreateGameHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();

    public CreateGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);

            int gameID = gameService.createGame(authToken, request.gameName());

            ctx.status(200);
            ctx.json(new CreateGameResult(gameID));

        } catch (DataAccessException e) {
            HandlerUtils.handleError(ctx, e);
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("unauthorized")) {
                ctx.status(401);
            } else {
                ctx.status(400);
            }
            ctx.json(new ErrorResult("Error: " + e.getMessage()));
        }
    }
}