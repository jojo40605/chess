package handler;

import io.javalin.http.Context;
import result.ErrorResult;
import service.ClearService;
import service.UnauthorizedException;
import dataaccess.DataAccessException;

public class ClearHandler {

    private final ClearService clearService;

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");

            // Clear data; service should throw UnauthorizedException if token invalid
            clearService.clear();

            ctx.status(200).result("{}");

        } catch (UnauthorizedException e) {
            ctx.status(401).json(new ErrorResult("Error: " + e.getMessage()));
        } catch (DataAccessException e) {
            ctx.status(500).json(new ErrorResult("Error: " + e.getMessage()));
        }
    }
}