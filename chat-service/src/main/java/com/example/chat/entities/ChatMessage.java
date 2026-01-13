package com.example.chat.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "msg_sender")
    private String senderId; // Cine trimite (UUID-ul userului sau "ADMIN")

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime timestamp;

    @Column(name = "msg_type")
    private String msgType;

    @Column(name = "user_id")
    private String userId; // Thread-ul de chat (ID-ul clientului implicat)

    @Column(name = "status")
    private String status = "OPEN"; // Implicit, mesajele noi sunt OPEN

    public ChatMessage() {}

    // Getters & Setters (fără sessionId)
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getMsgType() { return msgType; }
    public void setMsgType(String msgType) { this.msgType = msgType; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}