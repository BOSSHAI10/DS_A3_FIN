package com.example.chat.controllers;

import org.springframework.http.ResponseEntity;
import com.example.chat.entities.ChatMessage;
import com.example.chat.repositories.ChatMessageRepository;
import com.example.chat.services.RuleBasedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
public class ChatController {

    private final RuleBasedService ruleBasedService;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatController(RuleBasedService ruleBasedService,
                          ChatMessageRepository chatMessageRepository,
                          SimpMessagingTemplate messagingTemplate) {
        this.ruleBasedService = ruleBasedService;
        this.chatMessageRepository = chatMessageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // --- 1. WEBSOCKET: Mesaje de la CLIENT ---
    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage message) {
        System.out.println(">>> Mesaj primit de la: " + message.getSenderId());

        // CONFIGURARE ID-uri
        if (message.getId() == null) message.setId(UUID.randomUUID());
        message.setTimestamp(LocalDateTime.now());

        // --- FIX CRITIC: Sincronizăm user_id cu senderId ---
        // Fără această linie, mesajul ajunge la Admin cu userId = null
        message.setUserId(message.getSenderId());

        // 1. Salvăm mesajul clientului în DB
        chatMessageRepository.save(message);

        // 2. Generăm răspunsul automat
        String botResponseText = ruleBasedService.getRuleBasedResponse(message.getContent());

        // 3. LOGICA DE TICHET: Dacă e cerere pentru admin, trimitem la /topic/admin
        if (botResponseText.contains("ADMIN_REQUEST")) {
            // Trimitem obiectul 'message' completat (cu ID și Timestamp)
            messagingTemplate.convertAndSend("/topic/admin", message);
            System.out.println("!!! Mesaj trimis către topicul /topic/admin pentru: " + message.getSenderId());
        }

        // 4. Trimitere răspuns Bot către Client
        ChatMessage response = new ChatMessage();
        response.setId(UUID.randomUUID());
        response.setSenderId("System-AI");
        response.setUserId(message.getSenderId());
        response.setContent(botResponseText.replace("ADMIN_REQUEST:", ""));
        response.setTimestamp(LocalDateTime.now());
        response.setMsgType("BOT_RESPONSE");

        chatMessageRepository.save(response);
        messagingTemplate.convertAndSend("/topic/reply/" + message.getSenderId(), response);
    }
    // --- 2. WEBSOCKET: Mesaje de la ADMIN către User ---
    @MessageMapping("/admin/reply")
    public void processAdminReply(@Payload ChatMessage message) {
        // Adminul trimite un mesaj. 'userId' din mesaj este destinatarul (clientul)

        if (message.getId() == null) {
            message.setId(UUID.randomUUID()); // FĂRĂ .toString()
        }
        message.setTimestamp(LocalDateTime.now());
        message.setMsgType("ADMIN_REPLY");
        message.setSenderId("ADMIN");

        // 1. Salvăm în DB
        chatMessageRepository.save(message);

        // 2. Trimitem clientului specific pe topicul lui
        messagingTemplate.convertAndSend("/topic/reply/" + message.getUserId(), message);

        System.out.println("<<< Admin a răspuns lui: " + message.getUserId());
    }

    // --- 3. REST API: Endpoint-uri pentru Interfața Admin ---

    // Returnează lista userilor care au conversații
    @GetMapping("/chat/admin/users")
    public List<String> getUsersWithChats() {
        return chatMessageRepository.findDistinctUserIds();
    }

    // Returnează istoricul conversației cu un anumit user
    @GetMapping("/chat/admin/history/{userId}")
    public List<ChatMessage> getUserChatHistory(@PathVariable String userId) {
        return chatMessageRepository.findByUserIdOrderByTimestampAsc(userId);
    }

    @GetMapping("/chat/admin/requests")
    public List<ChatMessage> getAllRequests() {
        // Returnăm toate mesajele pentru a popula tabelul de tichete la încărcare
        // Admin.jsx are nevoie de acest endpoint pentru funcția fetchData()
        return chatMessageRepository.findAll();
    }

    @PostMapping("/chat/send")
    public ResponseEntity<Void> sendAdminReply(@RequestBody ChatMessage message) {
        // 1. Validăm că avem destinatarul (userId-ul clientului)
        if (message.getUserId() == null) {
            System.out.println("!!! Eroare: Adminul a trimis un mesaj fără userId!");
            return ResponseEntity.badRequest().build();
        }
        // 2. Completăm datele mesajului
        message.setId(UUID.randomUUID());
        message.setSenderId("ADMIN");
        message.setMsgType("ADMIN_REPLY");
        message.setTimestamp(LocalDateTime.now());
        chatMessageRepository.save(message);
        // 3. Salvăm în baza de date chat-db
        chatMessageRepository.save(message);

        // 4. TRIMITEM PRIN WEBSOCKET CĂTRE CLIENT
        // Clientul este abonat la: /topic/reply/{userId}
        String destination = "/topic/reply/" + message.getUserId();
        messagingTemplate.convertAndSend(destination, message);

        System.out.println("<<< Răspuns Admin trimis către: " + destination);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/chat/admin/resolve/{userId}")
    public ResponseEntity<Void> resolveTicket(@PathVariable String userId) {
        // Marcăm toate mesajele de la acest user ca fiind RESOLVED
        List<ChatMessage> messages = chatMessageRepository.findByUserIdOrderByTimestampAsc(userId);
        for (ChatMessage msg : messages) {
            msg.setStatus("RESOLVED");
        }
        chatMessageRepository.saveAll(messages);

        // Opțional: Trimitem un semnal prin WebSocket clientului că tichetul s-a închis
        ChatMessage closeMsg = new ChatMessage();
        closeMsg.setSenderId("SYSTEM");
        closeMsg.setUserId(userId);
        closeMsg.setContent("Tichetul a fost marcat ca rezolvat de un administrator.");
        closeMsg.setMsgType("TICKET_CLOSED");
        messagingTemplate.convertAndSend("/topic/reply/" + userId, closeMsg);

        return ResponseEntity.ok().build();
    }
}