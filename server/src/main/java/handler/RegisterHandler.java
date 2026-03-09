package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import request.RegisterRequest;
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
     */
    public void handle(Context ctx) {
        try {
            RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);

            // Validate required fields
            if (request.username() == null || request.password() == null || request.email() == null) {
                ctx.status(HttpStatus.BAD_REQUEST);
                ctx.json(new result.ErrorResult("Error: missing required fields"));
                return;
            }

            // Delegate registration to service
            var authData = userService.register(
                    request.username(),
                    request.password(),
                    request.email()
            );

            ctx.status(HttpStatus.OK);
            ctx.json(new RegisterResult(authData.username(), authData.authToken()));

        } catch (Exception e) {
            // Delegate exception handling to shared handler util
            HandlerUtils.handleError(ctx, e);
        }
    }
}