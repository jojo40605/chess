package client;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import jakarta.websocket.*;
import java.io.IOException;
import java.net.URI;

public class WebSocketFacade extends Endpoint {

    private Session session;
    private final ServerMessageObserver observer;
    private final Gson gson = new Gson();

    public WebSocketFacade(String url, ServerMessageObserver observer) throws Exception {
        this.observer = observer;
        try {
            // 1. Clean up the URL
            URI socketUri = cleanUrl(url);

            // 2. Establish connection
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.setDefaultMaxSessionIdleTimeout(600000);
            this.session = container.connectToServer(this, socketUri);

            // 3. Set the message handler (Logic moved to helper method)
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    processMessage(message);
                }
            });
        } catch (Exception e) {
            throw new Exception("Error: Failed to connect to WebSocket: " + e.getMessage());
        }
    }

    /**
     * Helper to flatten the constructor and handle URL logic.
     */
    private URI cleanUrl(String url) throws Exception {
        url = url.replace("http", "ws");
        if (!url.endsWith("/ws")) {
            url = url + "/ws";
        }
        return new URI(url);
    }

    /**
     * This handles the processing of messages outside of the constructor's nesting.
     */
    private void processMessage(String message) {
        try {
            ServerMessage baseMessage = gson.fromJson(message, ServerMessage.class);

            if (baseMessage == null || baseMessage.getServerMessageType() == null) {
                System.out.println("DEBUG: Received empty or malformed message from server.");
                return;
            }

            handleType(baseMessage.getServerMessageType(), message);
        } catch (Exception e) {
            System.err.println("CRITICAL: Error in WebSocket onMessage handling!");
            e.printStackTrace();
        }
    }

    /**
     * Further flattens the switch statement logic.
     */
    private void handleType(ServerMessage.ServerMessageType type, String message) {
        switch (type) {
            case LOAD_GAME -> {
                var msg = gson.fromJson(message, websocket.messages.LoadGameMessage.class);
                observer.notify(msg);
            }
            case NOTIFICATION -> {
                var msg = gson.fromJson(message, websocket.messages.NotificationMessage.class);
                observer.notify(msg);
            }
            case ERROR -> {
                var msg = gson.fromJson(message, websocket.messages.ErrorMessage.class);
                observer.notify(msg);
            }
            default -> System.out.println("DEBUG: Unknown message type: " + type);
        }
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        session.setMaxIdleTimeout(600000);
        System.out.println("DEBUG: WebSocket connection established!");
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

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("DEBUG: Code: " + closeReason.getCloseCode().getCode());
        System.out.println("DEBUG: Reason: " + closeReason.getReasonPhrase());
    }

    @Override
    public void onError(Session session, Throwable throwable) {
        // Log errors to console so you can see why the pipe broke
        System.err.println("WebSocket Error: " + throwable.getMessage());
    }
}