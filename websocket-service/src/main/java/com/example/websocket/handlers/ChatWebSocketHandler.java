package com.example.websocket.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Component
public class ChatWebSocketHandler implements WebSocketHandler {
    
    private static final Logger logger = Logger.getLogger(ChatWebSocketHandler.class.getName());
    
    // Store user sessions: userId -> WebSocketSession
    private final ConcurrentHashMap<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = extractUserId(session);
        if (userId != null) {
            userSessions.put(userId, session);
            logger.info("Chat WebSocket connection established for user: " + userId);
            
            // Send welcome message
            sendMessageToUser(userId, createMessage("system", "Conexiune chat stabilită!", "connection"));
        }
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // Handle incoming messages if needed
        String payload = message.getPayload().toString();
        logger.info("Received chat message: " + payload);
        
        // Echo back or process message
        String userId = extractUserId(session);
        if (userId != null) {
            sendMessageToUser(userId, createMessage("echo", "Am primit mesajul tău: " + payload, "received"));
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.severe("Chat WebSocket transport error: " + exception.getMessage());
        String userId = extractUserId(session);
        if (userId != null) {
            userSessions.remove(userId);
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String userId = extractUserId(session);
        if (userId != null) {
            userSessions.remove(userId);
            logger.info("Chat WebSocket connection closed for user: " + userId + ", status: " + closeStatus);
        }
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    public void sendMessageToUser(String userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                logger.info("Sent chat message to user " + userId + ": " + message);
            } catch (Exception e) {
                logger.severe("Error sending chat message to user " + userId + ": " + e.getMessage());
                userSessions.remove(userId);
            }
        } else {
            logger.warning("No active chat session for user: " + userId);
        }
    }
    
    public void broadcastToAllUsers(String message) {
        userSessions.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    logger.severe("Error broadcasting to user " + userId + ": " + e.getMessage());
                }
            }
        });
    }
    
    private String extractUserId(WebSocketSession session) {
        // Extract user ID from session attributes or query parameters
        String query = session.getUri().getQuery();
        if (query != null && query.contains("userId=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("userId=")) {
                    return param.substring(7);
                }
            }
        }
        return null;
    }
    
    private String createMessage(String sender, String content, String type) {
        try {
            ChatMessage msg = new ChatMessage(sender, content, type, System.currentTimeMillis());
            return objectMapper.writeValueAsString(msg);
        } catch (Exception e) {
            return "{\"error\":\"Failed to create message\"}";
        }
    }
    
    public int getConnectedUsersCount() {
        return userSessions.size();
    }
    
    // Simple message class
    private static class ChatMessage {
        public String sender;
        public String content;
        public String type;
        public long timestamp;
        
        public ChatMessage(String sender, String content, String type, long timestamp) {
            this.sender = sender;
            this.content = content;
            this.type = type;
            this.timestamp = timestamp;
        }
    }
}
