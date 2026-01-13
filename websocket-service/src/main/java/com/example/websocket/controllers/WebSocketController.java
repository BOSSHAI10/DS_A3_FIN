package com.example.websocket.controllers;

import com.example.websocket.services.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ws")
public class WebSocketController {
    
    @Autowired
    private WebSocketService webSocketService;
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("connectedChatUsers", webSocketService.getConnectedChatUsers());
        status.put("connectedNotificationUsers", webSocketService.getConnectedNotificationUsers());
        status.put("service", "WebSocket Service Running");
        return ResponseEntity.ok(status);
    }
    
    @PostMapping("/broadcast/notification")
    public ResponseEntity<String> broadcastNotification(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String level = request.getOrDefault("level", "info");
        
        if (message != null && !message.trim().isEmpty()) {
            webSocketService.sendSystemNotification(message, level);
            return ResponseEntity.ok("Notification broadcasted successfully");
        }
        
        return ResponseEntity.badRequest().body("Message cannot be empty");
    }
    
    @PostMapping("/send/notification")
    public ResponseEntity<String> sendNotificationToUser(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String message = request.get("message");
        
        if (userId != null && message != null && !message.trim().isEmpty()) {
            webSocketService.sendNotificationToUser(userId, message);
            return ResponseEntity.ok("Notification sent successfully");
        }
        
        return ResponseEntity.badRequest().body("UserId and message are required");
    }
    
    @PostMapping("/send/energy-alert")
    public ResponseEntity<String> sendEnergyAlert(@RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        String deviceId = (String) request.get("deviceId");
        Double consumption = (Double) request.get("consumption");
        
        if (userId != null && deviceId != null && consumption != null) {
            webSocketService.sendEnergyAlert(userId, deviceId, consumption);
            return ResponseEntity.ok("Energy alert sent successfully");
        }
        
        return ResponseEntity.badRequest().body("UserId, deviceId, and consumption are required");
    }
}
