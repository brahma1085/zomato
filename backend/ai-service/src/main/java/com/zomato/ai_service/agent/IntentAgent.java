package com.zomato.ai_service.agent;

import com.zomato.ai_service.dto.IntentResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class IntentAgent {

    private final ChatClient chatClient;

    public IntentAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public IntentResponse determineIntent(String query) {
        String systemPrompt = "You are an intelligent restaurant search assistant. " +
                "Determine the user's intent from the following query. " +
                "The intent could be SEARCH (looking for a specific restaurant by name), " +
                "RECOMMENDATION (asking for suggestions based on cuisine, vibe, or location), " +
                "or QUESTION (general inquiry). " +
                "Extract relevant parameters like cuisine, location, price, and vibe. " +
                "Respond strictly in JSON format with 'intentType' and 'extractedParameters'.\n\n" +
                "Query: " + (query != null ? query.replace("{", "[").replace("}", "]") : "");

        String aiResponse = chatClient.prompt().system(systemPrompt).call().content();
        
        IntentResponse response = new IntentResponse();
        response.setRawAiResponse(aiResponse);
        
        try {
            // Clean up potential markdown formatting from the response
            String cleanJson = aiResponse.replace("```json", "").replace("```", "").trim();
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(cleanJson);
            
            if (root.has("intentType")) {
                response.setIntentType(root.get("intentType").asText());
            } else {
                response.setIntentType("QUESTION"); // fallback
            }
            
            if (root.has("extractedParameters") && root.get("extractedParameters").isObject()) {
                Map<String, Object> params = mapper.convertValue(root.get("extractedParameters"), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                response.setExtractedParameters(params);
            } else {
                response.setExtractedParameters(new HashMap<>());
            }
        } catch (Exception e) {
            System.err.println("Failed to parse Intent JSON: " + e.getMessage());
            // Fallback logic
            if (aiResponse.toUpperCase().contains("RECOMMENDATION")) {
                response.setIntentType("RECOMMENDATION");
            } else if (aiResponse.toUpperCase().contains("SEARCH")) {
                response.setIntentType("SEARCH");
            } else {
                response.setIntentType("QUESTION");
            }
            response.setExtractedParameters(new HashMap<>());
        }

        return response;
    }
}
