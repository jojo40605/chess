//TODO add comments for quality
package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import request.LoginRequest;
import result.ErrorResult;
import result.LoginResult;
import service.UserService;

public class LoginHandler {

    private final UserService userService;
    private final Gson gson = new Gson();

    public LoginHandler(UserService userService) {
        this.userService = userService;
    }

    public void handle(Context ctx) {
        try {
            LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);

            // Check for missing fields FIRST
            if (request.username() == null || request.password() == null) {
                ctx.status(400);
                ctx.json(new ErrorResult("Error: bad request"));
                return;
            }

            var auth = userService.login(request.username(), request.password());
            ctx.status(200);
            ctx.json(new LoginResult(auth.username(), auth.authToken()));

        } catch (Exception e) {
            // Only return 401 if the fields were there but password was wrong
            ctx.status(401);
            ctx.json(new ErrorResult("Error: unauthorized"));
        }
    }
}