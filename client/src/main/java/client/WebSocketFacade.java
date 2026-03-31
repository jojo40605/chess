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
        try {
            // 1. Clean up the URL (Make it protocol-agnostic)
            url = url.replace("http", "ws");
            if (!url.endsWith("/ws")) {
                url = url + "/ws";
            }
            URI socketUri = new URI(url);
            this.observer = observer;

            // 2. Establish connection
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.setDefaultMaxSessionIdleTimeout(600000);
            this.session = container.connectToServer(this, socketUri);

            // 3. The "Detective" Message Handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    try {
                        // Pass 1: Parse as base class to find the type
                        ServerMessage baseMessage = gson.fromJson(message, ServerMessage.class);

                        if (baseMessage == null || baseMessage.getServerMessageType() == null) {
                            System.out.println("DEBUG: Received empty or malformed message from server.");
                            return;
                        }

                        // Pass 2: Re-parse into the actual specific class
                        switch (baseMessage.getServerMessageType()) {
                            case LOAD_GAME -> {
                                websocket.messages.LoadGameMessage loadMsg = gson.fromJson(message, websocket.messages.LoadGameMessage.class);
                                observer.notify(loadMsg);
                            }
                            case NOTIFICATION -> {
                                websocket.messages.NotificationMessage notificationMsg = gson.fromJson(message, websocket.messages.NotificationMessage.class);
                                observer.notify(notificationMsg);
                            }
                            case ERROR -> {
                                websocket.messages.ErrorMessage errorMsg = gson.fromJson(message, websocket.messages.ErrorMessage.class);
                                observer.notify(errorMsg);
                            }
                            default -> System.out.println("DEBUG: Unknown message type received: " + baseMessage.getServerMessageType());
                        }
                    } catch (Exception e) {
                        // THIS IS THE MOST IMPORTANT PART
                        System.err.println("CRITICAL: Error in WebSocket onMessage handling!");
                        e.printStackTrace();
                        // By catching this, we prevent the WebSocket thread from dying.
                    }
                }
            });
        } catch (Exception e) {
            throw new Exception("Error: Failed to connect to WebSocket: " + e.getMessage());
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