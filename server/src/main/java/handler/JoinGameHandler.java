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

            // 1. Basic Structure Check
            if (request == null || request.gameID() == null) {
                throw new BadRequestException("bad request");
            }

            // 2. Strict Color Check for Standard API Tests
            String color = request.playerColor();

            // The tests fail if color is null, empty, or not WHITE/BLACK.
            // Note: We allow "OBSERVER" here for your own client's functionality.
            if (color == null || color.isBlank()) {
                throw new BadRequestException("bad request");
            }

            boolean isWhite = color.equalsIgnoreCase("WHITE");
            boolean isBlack = color.equalsIgnoreCase("BLACK");
            boolean isObserver = color.equalsIgnoreCase("OBSERVER");

            if (!isWhite && !isBlack && !isObserver) {
                throw new BadRequestException("bad request");
            }

            // 3. Call Service
            gameService.joinGame(authToken, request.gameID(), color);

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