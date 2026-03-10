package handler;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import result.ErrorResult;
import service.BadRequestException;
import service.UnauthorizedException;
import service.ConflictException;
import dataaccess.DataAccessException;

/**
 * Utility class for consistent error handling across all handlers.
 */
public class HandlerUtils {

    /**
     * Handles exceptions by mapping them to correct HTTP status codes.
     *
     * @param ctx the Javalin context
     * @param e   the exception thrown by service or data layer
     */
    public static void handleError(Context ctx, Exception e) {
        switch (e) {
            case BadRequestException badRequestException ->
                    ctx.status(HttpStatus.BAD_REQUEST).json(new ErrorResult("Error: " + e.getMessage()));
            case UnauthorizedException unauthorizedException ->
                    ctx.status(HttpStatus.UNAUTHORIZED).json(new ErrorResult("Error: " + e.getMessage()));
            case ConflictException conflictException ->
                    ctx.status(HttpStatus.FORBIDDEN).json(new ErrorResult("Error: " + e.getMessage()));
            case DataAccessException dataAccessException ->
                    ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(new ErrorResult("Error: " + e.getMessage()));
            default ->
                // Fallback for unknown exceptions
                    ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(new ErrorResult("Error: " + e.getMessage()));
        }
    }
}