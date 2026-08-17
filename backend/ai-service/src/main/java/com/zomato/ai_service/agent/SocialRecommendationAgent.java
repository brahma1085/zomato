package com.zomato.ai_service.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SocialRecommendationAgent {

    private final ChatClient chatClient;

    public SocialRecommendationAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String analyzeSocialContext(List<String> socialConnections, List<String> candidateRestaurants) {
        if (socialConnections == null || socialConnections.isEmpty()) {
            return "No social context available.";
        }

        String systemPrompt = "You are a social recommendation agent. " +
                "Given the user's social connections: " + (socialConnections != null ? socialConnections.toString().replace("{", "[").replace("}", "]") : "") + " and candidate restaurants: " + (candidateRestaurants != null ? candidateRestaurants.toString().replace("{", "[").replace("}", "]") : "") + ", " +
                "simulate which of these restaurants are popular among their friends or have been highly rated by them. " +
                "Return a concise summary of social validation for the candidates.";

        return chatClient.prompt().system(systemPrompt).call().content();
    }
}
