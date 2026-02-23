package handler;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import result.ErrorResult;

/**
 * Utility class for shared handler functionality.
 * Helps reduce code duplication across the web layer.
 */
public class HandlerUtils {

    /**
     * Maps an exception to the correct HTTP status code and returns a JSON error result.
     * * @param ctx the Javalin context
     * @param e the exception to process
     */
    public static void handleError(Context ctx, Exception e) {
        String message = e.getMessage();

        if (message != null && message.toLowerCase().contains("unauthorized")) {
            ctx.status(HttpStatus.UNAUTHORIZED);
        } else if (message != null && message.toLowerCase().contains("already taken")) {
            ctx.status(HttpStatus.FORBIDDEN);
        } else {
            ctx.status(HttpStatus.BAD_REQUEST);
        }

        ctx.json(new ErrorResult("Error: " + message));
    }
}