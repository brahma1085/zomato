package com.zomato.ai_service.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BudgetAgent {

    private final ChatClient chatClient;

    public BudgetAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String analyzeBudget(String userId, List<String> recentOrders) {
        String systemPrompt = "You are a budget analysis agent. " +
                "Given the user's recent orders and spending: " + (recentOrders != null ? recentOrders.toString().replace("{", "[").replace("}", "]") : "") + ", " +
                "estimate their typical budget per meal (e.g., $, $$, $$$, $$$$). " +
                "Return just the budget string (e.g., $$) and a short one sentence explanation.";

        return chatClient.prompt().system(systemPrompt).call().content();
    }
}
