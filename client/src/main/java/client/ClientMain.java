package client;

public class ClientMain {
    public static void main(String[] args) {
        // Default to localhost:8080, but allow override via arguments
        var serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        System.out.println("♕ Welcome to 240 Chess Client ♕");

        // This starts the infinite loop
        new Repl(serverUrl).run();
    }
}