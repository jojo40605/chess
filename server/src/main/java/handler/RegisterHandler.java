package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import request.RegisterRequest;
import result.ErrorResult;
import result.RegisterResult;
import service.UserService;
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
                    request.username(),
                    request.password(),
                    request.email()
            );

            ctx.status(HttpStatus.OK);
            ctx.json(new RegisterResult(authData.username(), authData.authToken()));

        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && message.toLowerCase().contains("already taken")) {
                ctx.status(HttpStatus.FORBIDDEN);
                ctx.json(new ErrorResult("Error: " + message));
            } else if (e instanceof DataAccessException) {
                HandlerUtils.handleError(ctx, e);
            } else {
                ctx.status(HttpStatus.BAD_REQUEST);
                ctx.json(new ErrorResult("Error: " + message));
            }
        }
    }
}