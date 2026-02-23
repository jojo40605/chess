package handler;

import dataaccess.DataAccessException;
import io.javalin.http.Context;

public class ClearHandler {
    // Change this from DataAccess to ClearService
    private final service.ClearService clearService;

    public ClearHandler(service.ClearService clearService) {
        this.clearService = clearService;
    }

    public void handle(Context ctx) throws DataAccessException {
        clearService.clear();
        ctx.status(200);
        ctx.result("{}");
    }
}