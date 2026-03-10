package handler;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import result.ErrorResult;
import dataaccess.DataAccessException;

/**
 * Utility class for shared handler functionality.
 */
public class HandlerUtils {

    /**
     * Maps exceptions to proper HTTP status codes.
     * Server/database errors return 500; known errors get correct codes.
     */
    public static void handleError(Context ctx, Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "Unknown error";

        if (e instanceof DataAccessException) {
            // Database/server errors → 500
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        } else if (message.toLowerCase().contains("unauthorized")) {
            ctx.status(HttpStatus.UNAUTHORIZED);
        } else if (message.toLowerCase().contains("already taken")) {
            ctx.status(HttpStatus.FORBIDDEN);
        } else if (message.toLowerCase().contains("bad request")) {
            ctx.status(HttpStatus.BAD_REQUEST);
        } else {
            // Unknown exceptions → 500
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ctx.json(new ErrorResult("Error: " + message));
    }
}