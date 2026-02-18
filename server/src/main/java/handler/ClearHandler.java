package handler;

import io.javalin.http.Context;

public class ClearHandler {

    private final dataaccess.DataAccess dataAccess;

    public ClearHandler(dataaccess.DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void handle(Context ctx) {
        //FIXME: clear() not working
        //dataAccess.clear();
        ctx.status(200);
        ctx.result("{}");
    }
}