package server;

import dataaccess.*;
import handler.*;
import service.*;

import io.javalin.*;


public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // 1. Initialize DataAccess and Services
        DataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
        GameService gameService = new GameService(dataAccess);

        // 2. Initialize Handlers
        var registerHandler = new RegisterHandler(userService);
        var loginHandler = new LoginHandler(userService);
        var logoutHandler = new LogoutHandler(userService);
        var listGamesHandler = new ListGamesHandler(gameService);
        var createGameHandler = new CreateGameHandler(gameService);
        var joinGameHandler = new JoinGameHandler(gameService);
        var clearHandler = new ClearHandler(dataAccess);

        // 3. Register Endpoints

        // User & Session Endpoints
        javalin.post("/user", registerHandler::handle);
        javalin.post("/session", loginHandler::handle);
        javalin.delete("/session", logoutHandler::handle);

        // Game Endpoints
        javalin.get("/game", listGamesHandler::handle);
        javalin.post("/game", createGameHandler::handle);
        javalin.put("/game", joinGameHandler::handle);

        // Database/Clear Endpoint
        javalin.delete("/db", clearHandler::handle);

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
