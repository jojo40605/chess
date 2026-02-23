package handler;

import dataaccess.DataAccessException;
import io.javalin.http.Context;

/**
 * Handles HTTP requests to clear all data from the database.
 */
public class ClearHandler {

    private final dataaccess.DataAccess dataAccess;

    /**
     * Constructs a new ClearHandler with a specified DataAccess object.
     *
     * @param dataAccess the data access object used to interface with the database
     */
    public ClearHandler(dataaccess.DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    /**
     * Clears the database and returns a 200 OK status with an empty JSON object.
     *
     * @param ctx the Javalin context object representing the HTTP request and response
     * @throws DataAccessException if an error occurs during the database clear operation
     */
    public void handle(Context ctx) throws DataAccessException {
        dataAccess.clear();
        ctx.status(200);
        ctx.result("{}");
    }
}