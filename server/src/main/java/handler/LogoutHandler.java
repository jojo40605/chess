package handler;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import service.UserService;
import service.UnauthorizedException;
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

            ctx.status(HttpStatus.OK).result("{}");

        } catch (UnauthorizedException e) {
            ctx.status(HttpStatus.UNAUTHORIZED).json(new ErrorResult("Error: " + e.getMessage()));
        } catch (DataAccessException e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(new ErrorResult("Error: " + e.getMessage()));
        }
    }
}