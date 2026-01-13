package com.example.websocket.services;

import com.example.websocket.handlers.ChatWebSocketHandler;
import com.example.websocket.handlers.NotificationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {
    
    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;
    
    @Autowired
    private NotificationWebSocketHandler notificationWebSocketHandler;
    
    public void sendChatMessageToUser(String userId, String message) {
        chatWebSocketHandler.sendMessageToUser(userId, message);
    }
    
    public void broadcastChatMessage(String message) {
        chatWebSocketHandler.broadcastToAllUsers(message);
    }
    
    public void sendNotificationToUser(String userId, String notification) {
        notificationWebSocketHandler.sendNotificationToUser(userId, notification);
    }
    
    public void broadcastNotification(String notification) {
        notificationWebSocketHandler.broadcastNotification(notification);
    }
    
    public void sendEnergyAlert(String userId, String deviceId, double consumption) {
        notificationWebSocketHandler.sendEnergyAlert(userId, deviceId, consumption);
    }
    
    public void sendSystemNotification(String message, String level) {
        notificationWebSocketHandler.sendSystemNotification(message, level);
    }
    
    public int getConnectedChatUsers() {
        return chatWebSocketHandler.getConnectedUsersCount();
    }
    
    public int getConnectedNotificationUsers() {
        return notificationWebSocketHandler.getConnectedUsersCount();
    }
    
    public boolean isUserConnected(String userId) {
        return getConnectedChatUsers() > 0 || getConnectedNotificationUsers() > 0;
    }
}
