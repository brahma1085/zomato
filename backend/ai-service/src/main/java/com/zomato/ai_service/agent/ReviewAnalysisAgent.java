package com.zomato.ai_service.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReviewAnalysisAgent {

    private final ChatClient chatClient;

    public ReviewAnalysisAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String summarizeReviews(String restaurantId, List<String> recentReviews) {
        String systemPrompt = "You are a review analysis agent for a restaurant recommendation system. " +
                "Given the following recent reviews for restaurant ID " + restaurantId + ":\n" +
                (recentReviews != null ? String.join("\n- ", recentReviews).replace("{", "[").replace("}", "]") : "") + "\n\n" +
                "Summarize the general sentiment, highlighting food quality, service, ambience, and any recurring complaints. " +
                "Keep it under 3 sentences.";

        return chatClient.prompt().system(systemPrompt).call().content();
    }
}
