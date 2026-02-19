package server;

import com.google.gson.Gson;
import dataaccess.*;
import handler.*;
import io.javalin.*;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JsonMapper;
import org.jetbrains.annotations.NotNull;
import service.*;

import java.lang.reflect.Type;

public class Server {

    private final Javalin javalin;

    public Server() {
        Gson gson = new Gson();

        javalin = Javalin.create(config -> {
            // Fix 1: Properly map static files
            config.staticFiles.add("/web", Location.CLASSPATH);

            // Fix 2: Tell Javalin to use GSON instead of Jackson
            config.jsonMapper(new JsonMapper() {
                @NotNull
                @Override
                public String toJsonString(@NotNull Object obj, @NotNull Type type) {
                    return gson.toJson(obj);
                }

                @NotNull
                @Override
                public <T> T fromJsonString(@NotNull String json, @NotNull Type type) {
                    return gson.fromJson(json, type);
                }
            });
        });

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
        javalin.post("/user", registerHandler::handle);
        javalin.post("/session", loginHandler::handle);
        javalin.delete("/session", logoutHandler::handle);
        javalin.get("/game", listGamesHandler::handle);
        javalin.post("/game", createGameHandler::handle);
        javalin.put("/game", joinGameHandler::handle);
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