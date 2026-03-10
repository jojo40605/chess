package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import request.LoginRequest;
import result.ErrorResult;
import result.LoginResult;
import service.UserService;
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
                throw new IllegalArgumentException("bad request");
            }

            var authData = userService.login(request.username(), request.password());

            ctx.status(HttpStatus.OK);
            ctx.json(new LoginResult(authData.username(), authData.authToken()));

        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new ErrorResult("Error: " + e.getMessage()));
        } catch (DataAccessException e) {
            HandlerUtils.handleError(ctx, e);
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && message.toLowerCase().contains("unauthorized")) {
                ctx.status(HttpStatus.UNAUTHORIZED);
                ctx.json(new ErrorResult("Error: " + message));
            } else {
                ctx.status(HttpStatus.BAD_REQUEST);
                ctx.json(new ErrorResult("Error: " + message));
            }
        }
    }
}