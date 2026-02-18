package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import request.RegisterRequest;
import result.ErrorResult;
import result.RegisterResult;
import service.UserService;

public class RegisterHandler {

    private final UserService userService;
    private final Gson gson = new Gson();

    public RegisterHandler(UserService userService) {
        this.userService = userService;
    }

    public void handle(Context ctx) {
        try {
            RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);

            var auth = userService.register(
                    request.username(),
                    request.password(),
                    request.email()
            );

            ctx.status(200);
            ctx.json(new RegisterResult(auth.username(), auth.authToken()));

        } catch (Exception e) {
            if (e.getMessage().contains("already")) {
                ctx.status(403);
            } else {
                ctx.status(400);
            }
            ctx.json(new ErrorResult(e.getMessage()));
        }
    }
}