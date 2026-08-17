package com.zomato.ai_service.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExplanationAgent {

    private final ChatClient chatClient;

    public ExplanationAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String explainRecommendations(List<String> recommendedRestaurants, String userQuery, String userPreferences, String socialContext, String reviewSummaries) {
        String systemPrompt = "You are an explanation agent for a restaurant concierge. " +
                "Given the user's query: '" + (userQuery != null ? userQuery.replace("{", "[").replace("}", "]") : "") + "' and the top recommended restaurants: " + (recommendedRestaurants != null ? recommendedRestaurants.toString().replace("{", "[").replace("}", "]") : "") + ", " +
                "generate a friendly, conversational response explaining why these restaurants are a great fit.\n" +
                "You have access to the following context:\n" +
                "User Preferences: " + (userPreferences != null ? userPreferences.replace("{", "[").replace("}", "]") : "") + "\n" +
                "Social Context: " + (socialContext != null ? socialContext.replace("{", "[").replace("}", "]") : "") + "\n" +
                "Review Summaries: " + (reviewSummaries != null ? reviewSummaries.replace("{", "[").replace("}", "]") : "") + "\n\n" +
                "Mention some of these factors dynamically (e.g. 'Since you love Italian and your friend Alice rated it 5 stars...'). " +
                "Keep it under 4 sentences.";

        return chatClient.prompt().system(systemPrompt).call().content();
    }
}
