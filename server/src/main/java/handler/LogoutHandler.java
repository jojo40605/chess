package handler;

import dataaccess.DataAccessException;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import result.ErrorResult;
import service.UserService;

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
        }

        catch (DataAccessException e) {
            ctx.status(500);
            ctx.json(new ErrorResult("Error: " + e.getMessage()));
        }

        catch (Exception e) {
            HandlerUtils.handleError(ctx, e);
        }
    }
}