//TODO add comments for quality
package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import result.ErrorResult;
import service.UserService;

public class LogoutHandler {

    private final UserService userService;
    private final Gson gson = new Gson();

    public LogoutHandler(UserService userService) {
        this.userService = userService;
    }

    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("Authorization");
            userService.logout(authToken);

            ctx.status(200);
            ctx.result("{}");

        } catch (Exception e) {
            ctx.status(401);
            ctx.json(new ErrorResult(e.getMessage()));
        }
    }
}