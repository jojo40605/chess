package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import request.LoginRequest;
import result.LoginResult;
import result.ErrorResult;
import service.UserService;
import service.BadRequestException;
import service.UnauthorizedException;
import dataaccess.DataAccessException;

public class LoginHandler {

    private final UserService userService;
    private final Gson gson = new Gson();

    public LoginHandler(UserService userService) {
        this.userService = userService;
    }

    public void handle(Context ctx) {
        try {
            LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);

            if (request.username() == null || request.password() == null) {
                throw new BadRequestException("bad request");
            }

            var authData = userService.login(request.username(), request.password());

            ctx.status(HttpStatus.OK);
            ctx.json(new LoginResult(authData.username(), authData.authToken()));

        } catch (BadRequestException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResult("Error: " + e.getMessage()));
        } catch (UnauthorizedException e) {
            ctx.status(HttpStatus.UNAUTHORIZED).json(new ErrorResult("Error: " + e.getMessage()));
        } catch (DataAccessException e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(new ErrorResult("Error: " + e.getMessage()));
        }
    }
}