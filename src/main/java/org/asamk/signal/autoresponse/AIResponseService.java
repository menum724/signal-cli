package org.asamk.signal.autoresponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI service for generating responses using OpenAI-compatible APIs.
 */
public class AIResponseService {

    private static final Logger logger = LoggerFactory.getLogger(AIResponseService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final AutoResponseConfig config;
    private final HttpClient httpClient;
    private final List<Map<String, String>> conversationHistory;
    private static final int MAX_HISTORY_SIZE = 10;

    public AIResponseService(AutoResponseConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.conversationHistory = new ArrayList<>();
    }

    /**
     * Generate an AI response to a user message.
     *
     * @param userMessage The incoming message text
     * @param senderNumber The sender's phone number
     * @return The generated response
     * @throws IOException If the API call fails
     */
    public String generateResponse(String userMessage, String senderNumber) throws IOException {
        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            logger.warn("No API key configured, using fallback message");
            return config.getFallbackMessage();
        }

        try {
            return callAIAPI(userMessage);
        } catch (Exception e) {
            logger.error("Failed to generate AI response", e);
            if (config.isUseFallbackOnError()) {
                return config.getFallbackMessage();
            }
            throw new IOException("Failed to generate AI response", e);
        }
    }

    private String callAIAPI(String userMessage) throws IOException, InterruptedException {
        // Build the request payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", config.getModel());
        
        List<Map<String, String>> messages = new ArrayList<>();
        
        // Add system prompt
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", config.getSystemPrompt());
        messages.add(systemMessage);
        
        // Add conversation history (limited)
        if (!conversationHistory.isEmpty()) {
            int startIdx = Math.max(0, conversationHistory.size() - MAX_HISTORY_SIZE);
            messages.addAll(conversationHistory.subList(startIdx, conversationHistory.size()));
        }
        
        // Add current user message
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);
        
        payload.put("messages", messages);
        payload.put("max_tokens", config.getMaxResponseLength());
        payload.put("temperature", 0.7);

        String requestBody = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getApiEndpoint()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + config.getApiKey())
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        logger.debug("Calling AI API: {}", config.getApiEndpoint());
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            logger.error("AI API returned status {}: {}", response.statusCode(), response.body());
            throw new IOException("AI API returned status " + response.statusCode());
        }

        String responseText = parseAIResponse(response.body());
        
        // Update conversation history
        conversationHistory.add(userMsg);
        Map<String, String> assistantMsg = new HashMap<>();
        assistantMsg.put("role", "assistant");
        assistantMsg.put("content", responseText);
        conversationHistory.add(assistantMsg);
        
        // Keep history size limited
        while (conversationHistory.size() > MAX_HISTORY_SIZE * 2) {
            conversationHistory.remove(0);
        }
        
        return responseText;
    }

    private String parseAIResponse(String responseBody) throws IOException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.get("choices");
            
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null) {
                    JsonNode content = message.get("content");
                    if (content != null) {
                        return content.asText().trim();
                    }
                }
            }
            
            logger.warn("Unexpected API response format: {}", responseBody);
            throw new IOException("Unexpected API response format");
        } catch (Exception e) {
            logger.error("Failed to parse AI response", e);
            throw new IOException("Failed to parse AI response", e);
        }
    }

    /**
     * Clear conversation history.
     */
    public void clearHistory() {
        conversationHistory.clear();
        logger.debug("Conversation history cleared");
    }
}
