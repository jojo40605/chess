package handler;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import service.UserService;
import dataaccess.DataAccessException;
import result.ErrorResult;

public class LogoutHandler {

    private final UserService userService;

    public LogoutHandler(UserService userService) {
        this.userService = userService;
    }

    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");

            userService.logout(authToken);

            ctx.status(HttpStatus.OK);
            ctx.result("{}");

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