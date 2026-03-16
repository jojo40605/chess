package client;

import chess.ChessBoard;
import chess.ChessGame;
import model.GameData;
import result.ListGamesResult;
import result.RegisterResult;
import ui.BoardPrinter;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private String authToken = null;
    private State state = State.SIGNEDOUT;

    // Stores games from the last 'list' command so users can use index numbers (1, 2, 3...)
    private ArrayList<GameData> gameList = new ArrayList<>();

    public ChessClient(String serverUrl) {
        this.serverUrl = serverUrl;
        // Extracts the port from the URL if needed, but defaults to 8080
        this.server = new ServerFacade(8080);
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);

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
            return "Error: " + e.getMessage();
        }
    }

    private String login(String[] params) throws Exception {
        if (params.length == 2) {
            var auth = server.login(params[0], params[1]);
            authToken = auth.authToken();
            state = State.SIGNEDIN;
            return String.format("Logged in as %s.", auth.username());
        }
        throw new Exception("Expected: <USERNAME> <PASSWORD>");
    }

    private String register(String[] params) throws Exception {
        if (params.length == 3) {
            RegisterResult result = server.register(params[0], params[1], params[2]);
            this.authToken = result.authToken();
            this.state = State.SIGNEDIN;
            return String.format("Logged in as %s.", result.username());
        }
        throw new Exception("Expected: <USERNAME> <PASSWORD> <EMAIL>");
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
        throw new Exception("Expected: create <NAME>");
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
                int gameNumber = Integer.parseInt(params[0]);
                String color = params[1].toUpperCase();
                int gameID = gameList.get(gameNumber - 1).gameID();

                server.joinGame(authToken, color, gameID);

                // 1. Create a starting board
                ChessBoard board = new ChessBoard();
                board.resetBoard();

                // 2. Determine color for perspective
                ChessGame.TeamColor perspective = color.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;

                // 3. Print it!
                BoardPrinter.printBoard(board, perspective);

                return String.format("Joined game %d as %s.", gameNumber, color);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                throw new Exception("Invalid game number. Use 'list' to see valid numbers.");
            }
        }
        throw new Exception("Expected: join <NUMBER> [WHITE|BLACK]");
    }

    private String observeGame(String[] params) throws Exception {
        assertLoggedIn();
        if (params.length == 1) {
            try {
                int gameNumber = Integer.parseInt(params[0]);
                int gameID = gameList.get(gameNumber - 1).gameID();

                server.joinGame(authToken, null, gameID);

                // TODO: Add logic to draw the board here (Phase 5 requirement)
                return String.format("Observing game %d.", gameNumber);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                throw new Exception("Invalid game number.");
            }
        }
        throw new Exception("Expected: observe <NUMBER>");
    }

    private void assertLoggedIn() throws Exception {
        if (state == State.SIGNEDOUT) {
            throw new Exception("You must be logged in to do that.");
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
}