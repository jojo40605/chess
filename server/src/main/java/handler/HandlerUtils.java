package handler;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import dataaccess.DataAccessException;
import result.ErrorResult;

public class HandlerUtils {

    public static void handleError(Context ctx, Exception e) {
        if (e instanceof DataAccessException) {
            // Database/server errors → 500
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            String message = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

            if (message.contains("unauthorized")) {
                ctx.status(HttpStatus.UNAUTHORIZED);
            } else if (message.contains("already taken")) {
                ctx.status(HttpStatus.FORBIDDEN);
            } else {
                ctx.status(HttpStatus.BAD_REQUEST);
            }
        }

        ctx.json(new ErrorResult("Error: " + e.getMessage()));
    }
}