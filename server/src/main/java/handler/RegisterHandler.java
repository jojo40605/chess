package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import request.RegisterRequest;
import result.ErrorResult;
import result.RegisterResult;
import service.UserService;

/**
 * Handles HTTP requests to register a new user in the system.
 */
public class RegisterHandler {

    private final UserService userService;
    private final Gson gson = new Gson();

    public RegisterHandler(UserService userService) {
        this.userService = userService;
    }

    /**
     * Processes the registration request, returns user details and an auth token on success.
     * * @param ctx the Javalin context for the current HTTP request
     */
    public void handle(Context ctx) {
        try {
            RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);

            // Business logic delegation
            var authData = userService.register(
                    request.username(),
                    request.password(),
                    request.email()
            );

            ctx.status(HttpStatus.OK);
            ctx.json(new RegisterResult(authData.username(), authData.authToken()));

        } catch (Exception e) {
            handleError(ctx, e);
        }
    }

    /**
     * Maps service-layer exceptions to correct HTTP status codes.
     * Distinguishes between "Bad Request" (missing fields) and "Forbidden" (user exists).
     */
    private void handleError(Context ctx, Exception e) {
        String message = e.getMessage();

        if (message != null && message.toLowerCase().contains("already taken")) {
            ctx.status(HttpStatus.FORBIDDEN);
        } else {
            ctx.status(HttpStatus.BAD_REQUEST);
        }

        ctx.json(new ErrorResult("Error: " + message));
    }
}