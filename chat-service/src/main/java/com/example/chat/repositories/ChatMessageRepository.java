package com.example.chat.repositories;

import com.example.chat.entities.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID; // Import UUID

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> { // <--- UUID

    List<ChatMessage> findByUserIdOrderByTimestampAsc(String userId);

    @Query("SELECT DISTINCT c.userId FROM ChatMessage c WHERE c.userId IS NOT NULL")
    List<String> findDistinctUserIds();
}