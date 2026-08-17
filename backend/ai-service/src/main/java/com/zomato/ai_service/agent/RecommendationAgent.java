package com.zomato.ai_service.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class RecommendationAgent {

    private final ChatClient chatClient;

    public RecommendationAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String generateRecommendation(Map<String, Object> parameters, List<String> restaurantData) {
        String systemPrompt = "You are a friendly restaurant recommender. " +
                "The user is looking for a restaurant based on these preferences: " + (parameters != null ? parameters.toString().replace("{", "[").replace("}", "]") : "") + ". " +
                "Here is the data from our system matching those preferences: " + (restaurantData != null ? restaurantData.toString().replace("{", "[").replace("}", "]") : "") + ". " +
                "Generate a conversational response recommending the best option(s) from the provided data and explain why.";

        return chatClient.prompt()
                .system(systemPrompt)
                .user("What should I eat?")
                .call()
                .content();
    }

    public String generateAiNote(Map<String, Object> parameters, Map<String, Object> restaurant) {
        String systemPrompt = "You are a friendly and knowledgeable AI concierge for a food app. " +
                "The user is looking for a restaurant with these preferences: " + (parameters != null ? parameters.toString().replace("{", "[").replace("}", "]") : "") + ". " +
                "We are recommending this restaurant: " + (restaurant != null ? restaurant.toString().replace("{", "[").replace("}", "]") : "") + ". " +
                "Write a short, engaging 1-2 sentence note explaining why this restaurant is a great match for the user's preferences.";

        return chatClient.prompt()
                .system(systemPrompt)
                .user("Why are you recommending this restaurant?")
                .call()
                .content();
    }
}
