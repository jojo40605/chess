package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import request.JoinGameRequest;
import result.ErrorResult;
import service.*;
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

            // --- UPDATED VALIDATION ---
            if (request == null || request.gameID() == null) {
                throw new BadRequestException("bad request");
            }

            gameService.joinGame(authToken, request.gameID(), request.playerColor());

            ctx.status(HttpStatus.OK).result("{}");

        } catch (UnauthorizedException e) {
            ctx.status(HttpStatus.UNAUTHORIZED).json(new ErrorResult("Error: " + e.getMessage()));
        } catch (ForbiddenException e) {
            ctx.status(HttpStatus.FORBIDDEN).json(new ErrorResult("Error: " + e.getMessage()));
        } catch (BadRequestException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResult("Error: " + e.getMessage()));
        } catch (DataAccessException e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(new ErrorResult("Error: " + e.getMessage()));
        }
    }
}