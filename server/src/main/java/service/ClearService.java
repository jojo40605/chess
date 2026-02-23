package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;

/**
 * Service class for clearing all data from the database.
 */
public class ClearService {
    private final DataAccess dataAccess;

    public ClearService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    /**
     * Clears all users, games, and auth tokens from the system.
     * @throws DataAccessException if a database error occurs
     */
    public void clear() throws DataAccessException {
        dataAccess.clear();
    }
}