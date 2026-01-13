package com.example.chat.dtos;

import java.time.LocalDateTime;

public class ChatMessageDTO {
    private String id;
    private String userId;
    private String content;
    private String type;
    private String senderId;
    private String receiverId;
    private LocalDateTime timestamp;

    public ChatMessageDTO() {}

    // Păstrează getterii și setterii corespunzători, elimină getSessionId/setSessionId
}