package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import request.CreateGameRequest;
import result.CreateGameResult;
import result.ErrorResult;
import service.GameService;

/**
 * Handles HTTP requests to create a new chess game.
 * Prases the request body and validates the auth token.
 */
public class CreateGameHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();

    public CreateGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Processes the create game token
     * @param ctx the Javalin context object representing the HTTP request and response
     */
    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");

            CreateGameRequest request =
                    gson.fromJson(ctx.body(), CreateGameRequest.class);

            //call to the service layer
            int gameID =
                    gameService.createGame(authToken, request.gameName());

            ctx.status(200);
            ctx.json(new CreateGameResult(gameID));

        } catch (Exception e) {
            setStatus(ctx, e.getMessage());
            ctx.json(new ErrorResult(e.getMessage()));
        }
    }

    /**
     * Maps service-layer exceptions to appropriate HTTP status codes and JSON error responses.
     * @param ctx the Javalin context object
     * @param msg the JSON error response
     */
    private void setStatus(Context ctx, String msg) {
        if (msg.contains("unauthorized")){ctx.status(401);}
        else {ctx.status(400);}
    }
}