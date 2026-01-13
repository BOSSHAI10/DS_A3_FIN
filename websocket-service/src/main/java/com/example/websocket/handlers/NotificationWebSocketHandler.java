package com.example.websocket.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Component
public class NotificationWebSocketHandler implements WebSocketHandler {
    
    private static final Logger logger = Logger.getLogger(NotificationWebSocketHandler.class.getName());
    
    // Store user sessions: userId -> WebSocketSession
    private final ConcurrentHashMap<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = extractUserId(session);
        if (userId != null) {
            userSessions.put(userId, session);
            logger.info("Notification WebSocket connection established for user: " + userId);
            
            // Send welcome notification
            sendNotificationToUser(userId, createNotification("system", "Sistem de notificări conectat!", "info"));
        }
    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // Handle incoming messages (usually not needed for notifications)
        String payload = message.getPayload().toString();
        logger.info("Received notification message: " + payload);
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.severe("Notification WebSocket transport error: " + exception.getMessage());
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
            logger.info("Notification WebSocket connection closed for user: " + userId + ", status: " + closeStatus);
        }
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    public void sendNotificationToUser(String userId, String notification) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(notification));
                logger.info("Sent notification to user " + userId + ": " + notification);
            } catch (Exception e) {
                logger.severe("Error sending notification to user " + userId + ": " + e.getMessage());
                userSessions.remove(userId);
            }
        } else {
            logger.warning("No active notification session for user: " + userId);
        }
    }
    
    public void broadcastNotification(String notification) {
        userSessions.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(notification));
                } catch (Exception e) {
                    logger.severe("Error broadcasting notification to user " + userId + ": " + e.getMessage());
                }
            }
        });
    }
    
    public void sendEnergyAlert(String userId, String deviceId, double consumption) {
        String alert = createAlert("energy", "Alertă consum ridicat!", 
                                "Dispozitivul " + deviceId + " are un consum de: " + consumption + " kWh", 
                                "warning", deviceId, consumption);
        sendNotificationToUser(userId, alert);
    }
    
    public void sendSystemNotification(String message, String level) {
        String notification = createNotification("system", message, level);
        broadcastNotification(notification);
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
    
    private String createNotification(String source, String message, String level) {
        try {
            Notification notification = new Notification(source, message, level, System.currentTimeMillis());
            return objectMapper.writeValueAsString(notification);
        } catch (Exception e) {
            return "{\"error\":\"Failed to create notification\"}";
        }
    }
    
    private String createAlert(String type, String title, String message, String severity, String deviceId, double value) {
        try {
            Alert alert = new Alert(type, title, message, severity, deviceId, value, System.currentTimeMillis());
            return objectMapper.writeValueAsString(alert);
        } catch (Exception e) {
            return "{\"error\":\"Failed to create alert\"}";
        }
    }
    
    public int getConnectedUsersCount() {
        return userSessions.size();
    }
    
    // Simple notification classes
    private static class Notification {
        public String type = "notification";
        public String source;
        public String message;
        public String level;
        public long timestamp;
        
        public Notification(String source, String message, String level, long timestamp) {
            this.source = source;
            this.message = message;
            this.level = level;
            this.timestamp = timestamp;
        }
    }
    
    private static class Alert {
        public String type = "alert";
        public String alertType;
        public String title;
        public String message;
        public String severity;
        public String deviceId;
        public double value;
        public long timestamp;
        
        public Alert(String alertType, String title, String message, String severity, String deviceId, double value, long timestamp) {
            this.alertType = alertType;
            this.title = title;
            this.message = message;
            this.severity = severity;
            this.deviceId = deviceId;
            this.value = value;
            this.timestamp = timestamp;
        }
    }
}
