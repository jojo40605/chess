package websocket;

import com.google.gson.Gson;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import chess.ChessGame;
import chess.InvalidMoveException;
import model.AuthData;
import model.GameData;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.messages.ServerMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ErrorMessage;

import java.io.IOException;

public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();
    private final GameService gameService;

    public WebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void configure(WsConfig ws) {
        ws.onConnect(this::onConnect);
        ws.onMessage(this::onMessage);
    }

    private void onConnect(WsConnectContext ctx) {
        // We wait for the CONNECT command to associate the session with a game
    }

    private void onMessage(WsMessageContext ctx) {
        String message = ctx.message();
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

        switch (command.getCommandType()) {
            case CONNECT -> connect(ctx, command);
            case MAKE_MOVE -> makeMove(ctx, message);
            case LEAVE -> leave(ctx, command);
            case RESIGN -> resign(ctx, command);
        }
    }

    private void connect(WsMessageContext ctx, UserGameCommand command) {
        try {
            AuthData auth = validateToken(command.getAuthToken());
            GameData game = getGame(command.getGameID());

            // 1. Add this session to the manager
            connections.add(command.getGameID(), command.getAuthToken(), ctx.session);

            // 2. Send LOAD_GAME back to the person who joined
            ctx.send(gson.toJson(new LoadGameMessage(game)));

            // 3. Notify others
            String message;
            if (auth.username().equals(game.whiteUsername())) {
                message = String.format("%s joined the game as White", auth.username());
            } else if (auth.username().equals(game.blackUsername())) {
                message = String.format("%s joined the game as Black", auth.username());
            } else {
                message = String.format("%s joined the game as an observer", auth.username());
            }
            connections.broadcast(command.getGameID(), command.getAuthToken(), new NotificationMessage(message));

        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void makeMove(WsMessageContext ctx, String rawMessage) {
        try {
            // 1. Parse the specific Move Command
            MakeMoveCommand command = gson.fromJson(rawMessage, MakeMoveCommand.class);
            AuthData auth = validateToken(command.getAuthToken());
            GameData gameData = getGame(command.getGameID());
            ChessGame game = gameData.game();
            String username = auth.username();

            // 2. Fundamental Validations
            if (game.isGameOver()) {
                throw new Exception("Error: This game has already finished.");
            }

            // 3. Verify Player Identity (Are they White, Black, or just watching?)
            ChessGame.TeamColor playerColor = null;
            if (username.equals(gameData.whiteUsername())) {
                playerColor = ChessGame.TeamColor.WHITE;
            } else if (username.equals(gameData.blackUsername())) {
                playerColor = ChessGame.TeamColor.BLACK;
            } else {
                throw new Exception("Error: Observers cannot make moves.");
            }

            // 4. Verify Turn
            if (game.getTeamTurn() != playerColor) {
                throw new Exception("Error: It is not your turn.");
            }

            // 5. Attempt the move (This handles Chess rules like piece movement/captures)
            game.makeMove(command.getMove());

            // 6. Persistence: Update the database so the board is saved
            gameService.getDataAccess().updateGame(gameData);

            // 7. Broadcast: Tell everyone the board changed
            // (null excludeAuth sends the updated board to the person who moved too)
            var loadMessage = new LoadGameMessage(gameData);
            connections.broadcast(gameData.gameID(), null, loadMessage);

            // 8. Notify: Tell others what move was made (excludes the person who moved)
            String moveDesc = String.format("%s moved from %s to %s",
                    username,
                    command.getMove().getStartPosition(),
                    command.getMove().getEndPosition());
            connections.broadcast(gameData.gameID(), command.getAuthToken(), new NotificationMessage(moveDesc));

            // 9. Status Check: Is someone in Check or is the game over?
            checkGameStatus(gameData, username);

        } catch (Exception e) {
            // Sends the error only to the person who tried to make the illegal move
            sendError(ctx, e.getMessage());
        }
    }

    private void leave(WsMessageContext ctx, UserGameCommand command) {
        try {
            AuthData auth = validateToken(command.getAuthToken());
            GameData game = getGame(command.getGameID());

            // Remove from DB (if player)
            // if (auth.username().equals(game.whiteUsername())) { ... update DB ... }

            connections.remove(command.getGameID(), command.getAuthToken());
            String message = String.format("%s has left the game", auth.username());
            connections.broadcast(command.getGameID(), command.getAuthToken(), new NotificationMessage(message));

        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void resign(WsMessageContext ctx, UserGameCommand command) {
        try {
            AuthData auth = validateToken(command.getAuthToken());
            GameData gameData = getGame(command.getGameID());

            if (gameData.game().isGameOver()) {
                throw new Exception("Game is already over.");
            }

            // Verify the person resigning is a player, not an observer
            if (!auth.username().equals(gameData.whiteUsername()) && !auth.username().equals(gameData.blackUsername())) {
                throw new Exception("Observers cannot resign.");
            }

            gameData.game().setGameOver(true);
            // gameService.updateGame(gameData);

            String message = String.format("%s has resigned. The game is over.", auth.username());
            connections.broadcast(command.getGameID(), null, new NotificationMessage(message));

        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    // --- Helper Methods ---

    private AuthData validateToken(String token) throws Exception {
        // We use the same DataAccess logic your services use
        AuthData auth = gameService.getDataAccess().getAuth(token);
        if (auth == null) {
            throw new Exception("unauthorized");
        }
        return auth;
    }

    private GameData getGame(int gameID) throws Exception {
        GameData game = gameService.getDataAccess().getGame(gameID);
        if (game == null) {
            throw new Exception("Game ID is invalid.");
        }
        return game;
    }

    private void checkGameStatus(GameData gameData, String lastPlayer) throws IOException {
        ChessGame game = gameData.game();
        int gameID = gameData.gameID();
        String white = gameData.whiteUsername();
        String black = gameData.blackUsername();

        // Check for White's status
        if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
            game.setGameOver(true);
            connections.broadcast(gameID, null, new NotificationMessage(white + " is in checkmate. Game over!"));
        } else if (game.isInStalemate(ChessGame.TeamColor.WHITE)) {
            game.setGameOver(true);
            connections.broadcast(gameID, null, new NotificationMessage("Game ended in stalemate!"));
        } else if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
            connections.broadcast(gameID, null, new NotificationMessage(white + " is in check!"));
        }

        // Check for Black's status
        if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            game.setGameOver(true);
            connections.broadcast(gameID, null, new NotificationMessage(black + " is in checkmate. Game over!"));
        } else if (game.isInStalemate(ChessGame.TeamColor.BLACK)) {
            game.setGameOver(true);
            connections.broadcast(gameID, null, new NotificationMessage("Game ended in stalemate!"));
        } else if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
            connections.broadcast(gameID, null, new NotificationMessage(black + " is in check!"));
        }
    }

    private void sendError(WsMessageContext ctx, String message) {
        try {
            ctx.send(gson.toJson(new ErrorMessage(message)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}