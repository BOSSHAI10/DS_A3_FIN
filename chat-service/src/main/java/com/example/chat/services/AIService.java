package com.example.chat.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class AIService {

    // 1. Injectăm valorile exact cum le-ai definit în application.properties
    @Value("${google.ai.api-key}")
    private String apiKey;

    @Value("${google.ai.api-url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AIService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String generateResponse(String userMessage) {
        try {
            // 2. Construim URL-ul final adăugând parametrul key
            // apiUrl vine din properties: https://...:generateContent
            String finalUrl = apiUrl + "?key=" + apiKey;

            // 3. Construim corpul cererii JSON pentru Gemini
            // Structura: { "contents": [{ "parts": [{"text": "..."}] }] }
            Map<String, Object> textPart = new HashMap<>();

            // Context + Mesajul utilizatorului
            String prompt = "Ești un asistent energetic util și politicos. " +
                    "Răspunde scurt și la obiect în limba română la: " + userMessage;

            textPart.put("text", prompt);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", Collections.singletonList(textPart));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", Collections.singletonList(content));

            // 4. Setăm Header-ele
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 5. Trimitem cererea POST
            ResponseEntity<String> response = restTemplate.postForEntity(finalUrl, entity, String.class);

            // 6. Parsăm răspunsul
            return extractTextFromGeminiResponse(response.getBody());

        } catch (Exception e) {
            e.printStackTrace(); // Bun pentru debug în consolă
            return "Îmi pare rău, serviciul AI este momentan indisponibil. (Eroare internă)";
        }
    }

    private String extractTextFromGeminiResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            // Structura răspuns: candidates[0] -> content -> parts[0] -> text
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }
        } catch (Exception e) {
            System.err.println("Eroare la parsarea JSON Gemini: " + e.getMessage());
        }
        return "Nu am putut descifra răspunsul primit de la AI.";
    }
}