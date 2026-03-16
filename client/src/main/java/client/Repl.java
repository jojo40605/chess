package client;

import java.util.Scanner;
import static ui.EscapeSequences.*;

public class Repl {
    private final ChessClient client;

    public Repl(String serverUrl) {
        // The Repl creates the ChessClient to handle the 'brain' work
        this.client = new ChessClient(serverUrl);
    }

    public void run() {
        System.out.println(WHITE_QUEEN + " Welcome to 240 Chess. Type 'help' to get started. " + WHITE_QUEEN);

        Scanner scanner = new Scanner(System.in);
        var result = "";

        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result + RESET_TEXT_COLOR + "\n");
            } catch (Throwable e) {
                System.out.print(SET_TEXT_COLOR_RED + "Error: " + e.getMessage() + RESET_TEXT_COLOR + "\n");
            }
        }
    }

    private void printPrompt() {
        // Shows different prompts based on whether you are logged in
        String status = (client.getState() == State.SIGNEDIN) ? "[LOGGED_IN]" : "[LOGGED_OUT]";
        System.out.print("\n" + SET_TEXT_COLOR_WHITE + status + " >>> " + SET_TEXT_COLOR_GREEN);
    }
}