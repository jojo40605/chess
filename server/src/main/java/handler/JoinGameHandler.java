package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import request.JoinGameRequest;
import result.ErrorResult;
import service.GameService;
import dataaccess.DataAccessException;

public class JoinGameHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();

    public JoinGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handle(Context ctx) {
        try {
            JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);
            String authToken = ctx.header("Authorization");

            gameService.joinGame(authToken, request.gameID(), request.playerColor());

            ctx.status(200);
            ctx.result("{}");

        } catch (DataAccessException e) {
            HandlerUtils.handleError(ctx, e);
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("unauthorized")) {
                ctx.status(401);
            } else if (message.contains("already")) {
                ctx.status(403);
            } else {
                ctx.status(400);
            }
            ctx.json(new ErrorResult("Error: " + e.getMessage()));
        }
    }
}