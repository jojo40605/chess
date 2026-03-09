package handler;

import dataaccess.DataAccessException;
import io.javalin.http.Context;

public class ClearHandler {

    private final service.ClearService clearService;

    public ClearHandler(service.ClearService clearService) {
        this.clearService = clearService;
    }

    public void handle(Context ctx) {
        try {
            clearService.clear();
            ctx.status(200);
            ctx.result("{}");
        } catch (DataAccessException e) {
            ctx.status(500);
            ctx.result("{\"message\":\"Error: " + e.getMessage() + "\"}");
        }
    }
}