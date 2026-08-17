package com.zomato.ai_service.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RankingAgent {

    private final ChatClient chatClient;

    public RankingAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public List<String> rankCandidates(List<String> candidates, String userPreferences, String budget, String reviewSummaries, String socialContext, String query) {
        if (candidates == null || candidates.isEmpty()) {
            return candidates;
        }

        String systemPrompt = "You are a smart ranking agent. " +
                "Your job is to rank the following candidate restaurants from best match to worst match based on multiple factors.\n" +
                "Factors to consider:\n" +
                "1. User Query: " + (query != null ? query.replace("{", "[").replace("}", "]") : "") + "\n" +
                "2. User Preferences: " + (userPreferences != null ? userPreferences.replace("{", "[").replace("}", "]") : "") + "\n" +
                "3. Budget Constraints: " + (budget != null ? budget.replace("{", "[").replace("}", "]") : "") + "\n" +
                "4. Social Context (friends' favorites): " + (socialContext != null ? socialContext.replace("{", "[").replace("}", "]") : "") + "\n" +
                "5. Review Summaries: " + (reviewSummaries != null ? reviewSummaries.replace("{", "[").replace("}", "]") : "") + "\n\n" +
                "Candidates to rank: " + String.join(", ", candidates).replace("{", "[").replace("}", "]") + "\n\n" +
                "Output ONLY a comma-separated list of the restaurant names in ranked order from best to worst. Do not output anything else.";

        String aiResponse = chatClient.prompt().system(systemPrompt).call().content();
        if (aiResponse != null && !aiResponse.trim().isEmpty()) {
            return Arrays.stream(aiResponse.split(","))
                         .map(String::trim)
                         .collect(Collectors.toList());
        }
        return candidates;
    }
}
