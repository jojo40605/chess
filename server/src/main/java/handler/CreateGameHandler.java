package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import request.CreateGameRequest;
import result.CreateGameResult;
import result.ErrorResult;
import service.GameService;
import service.BadRequestException;
import service.UnauthorizedException;
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

            if (request.gameName() == null || request.gameName().isBlank()) {
                throw new BadRequestException("bad request");
            }

            int gameID = gameService.createGame(authToken, request.gameName());

            ctx.status(HttpStatus.OK);
            ctx.json(new CreateGameResult(gameID));

        } catch (BadRequestException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(new ErrorResult("Error: " + e.getMessage()));
        } catch (UnauthorizedException e) {
            ctx.status(HttpStatus.UNAUTHORIZED)
                    .json(new ErrorResult("Error: " + e.getMessage()));
        } catch (DataAccessException e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(new ErrorResult("Error: " + e.getMessage()));
        }
    }
}