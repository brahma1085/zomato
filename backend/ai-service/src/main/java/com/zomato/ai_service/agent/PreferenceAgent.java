package com.zomato.ai_service.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PreferenceAgent {

    private final ChatClient chatClient;

    public PreferenceAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String analyzePreferences(String userId, List<String> orderHistory) {
        String systemPrompt = "You are a preference analysis agent. " +
                "Given the user's past order history: " + (orderHistory != null ? orderHistory.toString().replace("{", "[").replace("}", "]") : "") + ", " +
                "extract their long-term preferences, favorite cuisines, and potential dietary restrictions. " +
                "Return a concise summary of their preferences.";

        return chatClient.prompt().system(systemPrompt).call().content();
    }
}
