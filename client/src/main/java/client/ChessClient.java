package client;

import chess.*;
import model.GameData;
import result.ListGamesResult;
import result.RegisterResult;
import ui.BoardPrinter;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;

public class ChessClient implements ServerMessageObserver{
    private final ServerFacade server;
    private WebSocketFacade ws;
    private final String serverUrl;
    private String authToken = null;
    private State state = State.SIGNEDOUT;

    private ArrayList<GameData> gameList = new ArrayList<>();
    // We need to remember which game and color we are for redrawing
    private Integer currentGameID = null;
    private ChessGame.TeamColor playerColor = null;
    private GameData lastGame = null;
    private boolean isObserver = false; // Add this line

    public ChessClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.server = new ServerFacade(8080);
        try {
            // Initialize the WS facade and tell it 'this' is the observer
            this.ws = new WebSocketFacade(serverUrl, this);
        } catch (Exception e) {
            System.out.println("WebSocket Error: " + e.getMessage());
        }
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);

            if (state == State.GAMEPLAY) {
                return switch (cmd) {
                    case "redraw" -> redrawBoard();
                    case "leave" -> leaveGame();
                    case "make" -> makeMove(params);
                    case "resign" -> resign();
                    case "highlight" -> highlight(params);
                    case "help" -> help();
                    default -> "Unknown gameplay command. Type 'help'.";
                };
            }

            return switch (cmd) {
                case "login" -> login(params);
                case "register" -> register(params);
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (Exception e) {
            // Cleaned up: server-side errors already start with "Error: "
            return e.getMessage();
        }
    }

    private String login(String[] params) throws Exception {
        if (params.length == 2) {
            var auth = server.login(params[0], params[1]);
            authToken = auth.authToken();
            state = State.SIGNEDIN;
            return String.format("Logged in as %s.", auth.username());
        }
        throw new Exception("Error: Expected <USERNAME> <PASSWORD>");
    }

    private String register(String[] params) throws Exception {
        if (params.length == 3) {
            RegisterResult result = server.register(params[0], params[1], params[2]);
            this.authToken = result.authToken();
            this.state = State.SIGNEDIN;
            return String.format("Logged in as %s.", result.username());
        }
        throw new Exception("Error: Expected <USERNAME> <PASSWORD> <EMAIL>");
    }

    private String logout() throws Exception {
        assertLoggedIn();
        server.logout(authToken);
        authToken = null;
        state = State.SIGNEDOUT;
        return "Logged out successfully.";
    }

    private String createGame(String[] params) throws Exception {
        assertLoggedIn();
        if (params.length == 1) {
            server.createGame(authToken, params[0]);
            return String.format("Game '%s' created successfully!", params[0]);
        }
        throw new Exception("Error: Expected create <NAME>");
    }

    private String listGames() throws Exception {
        assertLoggedIn();
        ListGamesResult result = server.listGames(authToken);
        gameList = new ArrayList<>(result.games());

        if (gameList.isEmpty()) {
            return "No games available. Create one!";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Current Games:\n");
        for (int i = 0; i < gameList.size(); i++) {
            var game = gameList.get(i);
            sb.append(String.format("  %d. %s (White: %s, Black: %s)\n",
                    i + 1,
                    game.gameName(),
                    game.whiteUsername() != null ? game.whiteUsername() : "empty",
                    game.blackUsername() != null ? game.blackUsername() : "empty"));
        }
        return sb.toString();
    }

    private String joinGame(String[] params) throws Exception {
        assertLoggedIn();
        if (params.length >= 2) {
            try {
                // 1. Parse the input
                int gameNumber = Integer.parseInt(params[0]);
                String colorStr = params[1].toUpperCase();
                int gameID = gameList.get(gameNumber - 1).gameID();

                // 2. HTTP Call: Tell the server to put our name in the database slot
                // This will throw an exception if the color is already taken (403 Forbidden)
                server.joinGame(authToken, colorStr, gameID);

                // 3. Update Local State: Remember who we are for this session
                this.currentGameID = gameID;
                this.isObserver = false; // Set to false here
                this.playerColor = colorStr.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
                this.state = State.GAMEPLAY; // Switches your eval() switch to gameplay mode

                // 4. WebSocket Call: Open the live connection
                // We send a CONNECT command so the server starts pushing board updates to us
                ws.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));

                return "Joined game successfully! Waiting for board...";

            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                throw new Exception("Error: Invalid game number. Use 'list' to see valid numbers.");
            }
        }
        throw new Exception("Error: Expected join <NUMBER> [WHITE|BLACK]");
    }

    private String observeGame(String[] params) throws Exception {
        assertLoggedIn();
        if (params.length == 1) {
            try {
                int gameNumber = Integer.parseInt(params[0]);
                int gameID = gameList.get(gameNumber - 1).gameID();

                this.currentGameID = gameID;
                this.playerColor = ChessGame.TeamColor.WHITE; // Observers see White on bottom
                this.state = State.GAMEPLAY;
                this.isObserver = true; // Set to true here

                ws.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));

                return "Observing game...";
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                throw new Exception("Error: Invalid game number.");
            }
        }
        throw new Exception("Error: Expected observe <NUMBER>");
    }

    private void assertLoggedIn() throws Exception {
        if (state == State.SIGNEDOUT) {
            throw new Exception("Error: You must be logged in to do that.");
        }
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                login <USERNAME> <PASSWORD> - to play chess
                quit - exit the program
                help - with possible commands
                """;
        }
        if (state == State.GAMEPLAY) {
            if (isObserver) {
                return """
            redraw - to see the board again
            leave - return to lobby
            help - see commands
            """;
            } else {
                return """
            redraw - to see the board again
            make <START> <END> - to move
            highlight <POSITION> - see legal moves
            resign - forfeit the game
            leave - return to lobby
            help - see commands
            """;
            }
        }
        return """
            create <NAME> - a game
            list - games
            join <NUMBER> [WHITE|BLACK] - a game
            observe <NUMBER> - a game
            logout - when you are done
            quit - exit the program
            help - with possible commands
            """;
    }

    public State getState() { return state; }

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                // We must cast the general message to the specific LoadGameMessage
                websocket.messages.LoadGameMessage loadMessage = (websocket.messages.LoadGameMessage) message;
                // Save this game locally so we can redraw or highlight later
                this.lastGame = loadMessage.getGame();

                System.out.println("\n");
                BoardPrinter.printBoard(lastGame.game().getBoard(), this.playerColor);
                printPrompt();
            }
            case NOTIFICATION -> {
                websocket.messages.NotificationMessage notification = (websocket.messages.NotificationMessage) message;
                System.out.println("\n" + notification.getMessage());
                printPrompt();
            }
            case ERROR -> {
                websocket.messages.ErrorMessage error = (websocket.messages.ErrorMessage) message;
                System.out.println("\n" + error.getErrorMessage());
                printPrompt();
            }
        }
    }

    // Small helper to keep the UI clean
    private void printPrompt() {
        System.out.print("\n[GAMEPLAY] >>> ");
    }

    private ChessPosition parsePosition(String posStr) throws IllegalArgumentException {
        if (posStr == null || posStr.length() != 2) {
            throw new IllegalArgumentException("Invalid coordinate format.");
        }

        // Convert column: 'a' -> 1, 'b' -> 2, ..., 'h' -> 8
        char colChar = Character.toLowerCase(posStr.charAt(0));
        int col = colChar - 'a' + 1;

        // Convert row: '1' -> 1, '2' -> 2, ..., '8' -> 8
        char rowChar = posStr.charAt(1);
        int row = Character.getNumericValue(rowChar);

        if (col < 1 || col > 8 || row < 1 || row > 8) {
            throw new IllegalArgumentException("Coordinates out of bounds.");
        }

        return new ChessPosition(row, col);
    }

    private String makeMove(String[] params) throws Exception {
        assertLoggedIn();

        if (isObserver) {
            throw new Exception("Error: You are observing this game and cannot make moves.");
        }

        // Validate basic input length
        if (params.length < 2) {
            throw new Exception("Error: Expected format 'make <START> <END> [PROMOTION]' (e.g., make e2 e4)");
        }

        try {
            // 1. Parse Positions using the helper above
            ChessPosition start = parsePosition(params[0]);
            ChessPosition end = parsePosition(params[1]);

            // 2. Determine Promotion Piece (if applicable)
            ChessPiece.PieceType promotion = null;
            if (params.length >= 3) {
                try {
                    // Uses the PieceType enum inside your ChessPiece class
                    promotion = ChessPiece.PieceType.valueOf(params[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new Exception("Error: Invalid promotion piece. Use QUEEN, ROOK, BISHOP, or KNIGHT.");
                }
            }

            // 3. Create the Move object
            ChessMove move = new ChessMove(start, end, promotion);

            // 4. Send via WebSocket
            // Note: We use the currentGameID saved during the join/observe command
            ws.sendCommand(new MakeMoveCommand(authToken, currentGameID, move));

            return String.format("Move submitted: %s to %s", params[0], params[1]);

        } catch (IllegalArgumentException e) {
            throw new Exception("Error: " + e.getMessage());
        }
    }

    private String redrawBoard() throws Exception {
        if (lastGame == null) throw new Exception("Error: No game state available to redraw.");
        BoardPrinter.printBoard(lastGame.game().getBoard(), this.playerColor);
        return "";
    }
    private String leaveGame() throws Exception {
        ws.sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, currentGameID));
        this.state = State.SIGNEDIN; // Return to the post-login menu
        this.currentGameID = null;
        return "Left the game.";
    }
    private String resign() throws Exception {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        String response = scanner.nextLine().toLowerCase();

        if (response.equals("yes")) {
            ws.sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, currentGameID));
            return "Resignation sent.";
        }
        return "Resignation cancelled.";
    }
    private String highlight(String[] params) throws Exception {
        if (params.length < 1) throw new Exception("Error: Expected highlight <POSITION>");
        if (lastGame == null) throw new Exception("Error: No game loaded yet.");

        ChessPosition pos = parsePosition(params[0]);

        // Use the game object stored in our lastGame variable
        Collection<ChessMove> validMoves = lastGame.game().validMoves(pos);

        // Check for null FIRST to avoid the error you're seeing
        if (validMoves == null || validMoves.isEmpty()) {
            return "No legal moves for the piece at " + params[0];
        }

        BoardPrinter.printBoardWithHighlights(lastGame.game().getBoard(), this.playerColor, pos, validMoves);

        return "";
    }

}