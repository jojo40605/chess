package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import request.LoginRequest;
import result.ErrorResult;
import result.LoginResult;
import service.UserService;

/**
 * Handles HTTP requests for user authentication (Login).
 * Converts JSON requests into LoginRequest objects and returns LoginResult on success.
 */
public class LoginHandler {

    private final UserService userService;
    private final Gson gson = new Gson();

    public LoginHandler(UserService userService) {
        this.userService = userService;
    }

    /**
     * Processes the login request.
     * * @param ctx the Javalin context for the current HTTP request
     */
    public void handle(Context ctx) {
        try {
            LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);
            validateRequest(request);

            var authData = userService.login(request.username(), request.password());

            ctx.status(HttpStatus.OK);
            ctx.json(new LoginResult(authData.username(), authData.authToken()));

        } catch (IllegalArgumentException e) {
            // Specifically handles the "bad request" (missing fields) case
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(new ErrorResult("Error: " + e.getMessage()));
        } catch (Exception e) {
            // Handles authentication failures or server errors
            handleError(ctx, e);
        }
    }

    private void validateRequest(LoginRequest request) {
        if (request.username() == null || request.password() == null) {
            throw new IllegalArgumentException("bad request");
        }
    }

    private void handleError(Context ctx, Exception e) {
        String message = e.getMessage();
        if (message != null && message.toLowerCase().contains("unauthorized")) {
            ctx.status(HttpStatus.UNAUTHORIZED);
        } else {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        ctx.json(new ErrorResult("Error: " + message));
    }
}