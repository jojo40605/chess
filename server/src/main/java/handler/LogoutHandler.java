package handler;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import result.ErrorResult;
import service.UserService;

/**
 * Handles HTTP requests to log out a user by invalidating their authentication token.
 */
public class LogoutHandler {

    private final UserService userService;

    public LogoutHandler(UserService userService) {
        this.userService = userService;
    }

    /**
     * Processes the logout request.
     * Extracts the auth token from the header and delegates to the service layer.
     * * @param ctx the Javalin context for the current HTTP request
     */
    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");

            // Business logic call to the service layer
            userService.logout(authToken);

            ctx.status(HttpStatus.OK);
            ctx.result("{}");

        } catch (Exception e) {
            HandlerUtils.handleError(ctx, e);
        }
    }
}