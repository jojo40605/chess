package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import request.RegisterRequest;
import result.RegisterResult;
import result.ErrorResult;
import service.UserService;
import service.BadRequestException;
import service.ConflictException;
import dataaccess.DataAccessException;

public class RegisterHandler {

    private final UserService userService;
    private final Gson gson = new Gson();

    public RegisterHandler(UserService userService) {
        this.userService = userService;
    }

    public void handle(Context ctx) {
        try {
            RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);
            var authData = userService.register(
                    request.username(), request.password(), request.email()
            );

            ctx.status(HttpStatus.OK);
            ctx.json(new RegisterResult(authData.username(), authData.authToken()));

        } catch (BadRequestException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResult("Error: " + e.getMessage()));
        } catch (ConflictException e) {
            ctx.status(HttpStatus.FORBIDDEN).json(new ErrorResult("Error: " + e.getMessage()));
        } catch (DataAccessException e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(new ErrorResult("Error: " + e.getMessage()));
        }
    }
}