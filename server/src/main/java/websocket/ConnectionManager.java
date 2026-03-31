package websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    // Maps GameID -> List of active connections for that game
    public final ConcurrentHashMap<Integer, ArrayList<Connection>> connections = new ConcurrentHashMap<>();

    public void add(Integer gameID, String authToken, Session session) {
        var connection = new Connection(authToken, session);
        connections.computeIfAbsent(gameID, k -> new ArrayList<>()).add(connection);
    }

    public void remove(Integer gameID, String authToken) {
        var gameConnections = connections.get(gameID);
        if (gameConnections != null) {
            gameConnections.removeIf(c -> c.authToken.equals(authToken));
        }
    }

    /**
     * Sends a message to everyone in the game EXCEPT the root client.
     */
    public void broadcast(Integer gameID, String excludeAuthToken, ServerMessage notification) throws IOException {
        var gameConnections = connections.get(gameID);
        if (gameConnections != null) {
            for (var c : gameConnections) {
                if (c.session.isOpen()) {
                    if (!c.authToken.equals(excludeAuthToken)) {
                        c.send(new Gson().toJson(notification));
                    }
                }
            }
        }
    }
}