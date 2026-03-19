package client;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import jakarta.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    private Session session;
    private final ServerMessageObserver observer;
    private final Gson gson = new Gson();

    public WebSocketFacade(String url, ServerMessageObserver observer) throws Exception {
        try {
            url = url.replace("http", "ws");
            URI socketUri = new URI(url + "/ws");
            this.observer = observer;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketUri);

            // Set up a message handler to receive messages from the server
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    // Convert JSON back to a ServerMessage and tell the observer (UI)
                    ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
                    observer.notify(serverMessage);
                }
            });
        } catch (DeploymentException | URISyntaxException | IOException e) {
            throw new Exception("Error: Failed to connect to WebSocket: " + e.getMessage());
        }
    }

    // Standard Endpoint method - called when connection is established
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    /**
     * Sends a command (CONNECT, MAKE_MOVE, etc.) to the server.
     */
    public void sendCommand(UserGameCommand command) throws Exception {
        try {
            this.session.getBasicRemote().sendText(gson.toJson(command));
        } catch (IOException e) {
            throw new Exception("Error: Failed to send command: " + e.getMessage());
        }
    }
}