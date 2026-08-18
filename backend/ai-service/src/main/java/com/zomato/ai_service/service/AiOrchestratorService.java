package com.zomato.ai_service.service;

import com.zomato.ai_service.agent.*;
import com.zomato.ai_service.client.RecommendationServiceClient;
import com.zomato.ai_service.client.ReviewServiceClient;
import com.zomato.ai_service.client.UserServiceClient;
import com.zomato.ai_service.dto.ChatResponse;
import com.zomato.ai_service.dto.IntentResponse;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AiOrchestratorService {

    private static final Logger logger = LoggerFactory.getLogger(AiOrchestratorService.class);

    private final IntentAgent intentAgent;
    private final PreferenceAgent preferenceAgent;
    private final BudgetAgent budgetAgent;
    private final ReviewAnalysisAgent reviewAnalysisAgent;
    private final SocialRecommendationAgent socialRecommendationAgent;
    private final RankingAgent rankingAgent;
    private final ExplanationAgent explanationAgent;
    private final RecommendationServiceClient recommendationServiceClient;
    private final ReviewServiceClient reviewServiceClient;
    private final UserServiceClient userServiceClient;
    private final RecommendationAgent recommendationAgent;

    public AiOrchestratorService(
            IntentAgent intentAgent,
            PreferenceAgent preferenceAgent,
            BudgetAgent budgetAgent,
            ReviewAnalysisAgent reviewAnalysisAgent,
            SocialRecommendationAgent socialRecommendationAgent,
            RankingAgent rankingAgent,
            ExplanationAgent explanationAgent,
            RecommendationServiceClient recommendationServiceClient,
            ReviewServiceClient reviewServiceClient,
            UserServiceClient userServiceClient,
            RecommendationAgent recommendationAgent) {
        this.intentAgent = intentAgent;
        this.preferenceAgent = preferenceAgent;
        this.budgetAgent = budgetAgent;
        this.reviewAnalysisAgent = reviewAnalysisAgent;
        this.socialRecommendationAgent = socialRecommendationAgent;
        this.rankingAgent = rankingAgent;
        this.explanationAgent = explanationAgent;
        this.recommendationServiceClient = recommendationServiceClient;
        this.reviewServiceClient = reviewServiceClient;
        this.userServiceClient = userServiceClient;
        this.recommendationAgent = recommendationAgent;
    }

    public ChatResponse orchestrateChat(List<String> userIds, String query, List<String> history, Double lat, Double lng) {
        try {
            // Step 1: Parse Intent (with history for conversational context)
            String safeQuery = query != null ? query : "";
            String contextQuery = safeQuery;
            if (history != null && !history.isEmpty()) {
                contextQuery = "Previous context: " + String.join(" | ", history) + ". New query: " + safeQuery;
            }
            if (contextQuery.trim().isEmpty()) {
                return new ChatResponse("Please provide a search query or a question.", Collections.emptyList());
            }
            IntentResponse intent = intentAgent.determineIntent(contextQuery);

            // Step 2: Fetch Context via Context Agents
            StringBuilder combinedPreferences = new StringBuilder();
            StringBuilder combinedBudget = new StringBuilder();
            List<String> combinedSocialConnections = new java.util.ArrayList<>();
            
            for (String userId : userIds) {
                Map<String, Object> userProfile = new HashMap<>();
                try {
                    userProfile = userServiceClient.getUserProfile(userId);
                } catch (Exception e) {
                    logger.error("Could not fetch user profile for {}", userId);
                }
                
                List<String> orderHistory = (List<String>) userProfile.getOrDefault("dietaryPreferences", Collections.emptyList());
                List<String> socialConnections = (List<String>) userProfile.getOrDefault("socialConnections", Collections.emptyList());
                combinedSocialConnections.addAll(socialConnections);
                
                combinedPreferences.append("User ").append(userId).append(": ").append(preferenceAgent.analyzePreferences(userId, orderHistory)).append("\n");
                combinedBudget.append("User ").append(userId).append(": ").append(budgetAgent.analyzeBudget(userId, orderHistory)).append("\n");
            }
            
            String preferences = combinedPreferences.toString();
            String budget = combinedBudget.toString();

            // Step 3: Fetch initial candidates (Semantic Search via recommendation-service for primary user)
            String primaryUserId = userIds.isEmpty() ? "default-user" : userIds.get(0);
            
            // Extract location from intent parameters, if available
            String location = null;
            if (intent.getExtractedParameters() != null && intent.getExtractedParameters().containsKey("location")) {
                location = String.valueOf(intent.getExtractedParameters().get("location"));
            }
            
            List<Map<String, Object>> candidateRestaurantsObj = recommendationServiceClient.getRecommendations(primaryUserId, query, location, lat, lng);
            
            if (candidateRestaurantsObj == null || candidateRestaurantsObj.isEmpty()) {
                return new ChatResponse("I couldn't find any exact matches for that in your area. Could you try broadening your search or choosing a different cuisine?", Collections.emptyList());
            }

            List<String> candidateRestaurants = candidateRestaurantsObj.stream()
                    .map(r -> (String) r.getOrDefault("name", r.getOrDefault("restaurantName", "Unknown Restaurant")))
                    .collect(Collectors.toList());

            // Step 4: Context Augmentation (Reviews & Social)
            Map<String, String> reviewSummaries = new HashMap<>();
            for (Map<String, Object> candidate : candidateRestaurantsObj) {
                String restaurantName = (String) candidate.getOrDefault("name", candidate.getOrDefault("restaurantName", "Unknown Restaurant"));
                try {
                    String idStr = String.valueOf(candidate.get("id"));
                    // Use a fallback ID if the parsed ID is not valid or if parsing fails
                    Long restaurantId = 1L; 
                    try {
                        // Extract numeric ID if the ID string contains UUID or strings
                        if (idStr != null && idStr.matches("-?\\d+")) {
                            restaurantId = Long.parseLong(idStr);
                        } else {
                            // Using hash of string ID if UUID to get a mock numeric ID just for review API backward compatibility. 
                            // Note: review service usually takes Long. If DB uses Long, parsing will succeed.
                            restaurantId = (long) Math.abs(idStr.hashCode());
                        }
                    } catch (NumberFormatException ignored) {}

                    List<Map<String, Object>> reviews = reviewServiceClient.getReviewsForRestaurant(restaurantId);
                    List<String> reviewComments = reviews.stream()
                            .map(r -> r.getOrDefault("comment", "").toString())
                            .toList();
                    
                    if (!reviewComments.isEmpty()) {
                        String reviewSummary = reviewAnalysisAgent.summarizeReviews(idStr, reviewComments);
                        reviewSummaries.put(restaurantName, reviewSummary);
                    }
                } catch (Exception e) {
                     // Fallback
                }
            }
            
            String aggregatedReviews = reviewSummaries.toString();
            String socialContext = socialRecommendationAgent.analyzeSocialContext(combinedSocialConnections, candidateRestaurants);

            // Step 5: Ranking
            List<String> rankedCandidates = rankingAgent.rankCandidates(candidateRestaurants, preferences, budget, aggregatedReviews, socialContext, query);

            // Step 6: Explanation
            String explanation = explanationAgent.explainRecommendations(rankedCandidates, query, preferences, socialContext, aggregatedReviews);

            // Match ranked candidate names back to their objects and re-order them
            List<Map<String, Object>> finalCandidates = new java.util.ArrayList<>();
            for (String rankedName : rankedCandidates) {
                candidateRestaurantsObj.stream()
                        .filter(r -> rankedName.equals(r.getOrDefault("name", r.get("restaurantName"))))
                        .findFirst()
                        .ifPresent(finalCandidates::add);
            }
            
            // If the ranking agent didn't return matches or failed, fallback to original
            if (finalCandidates.isEmpty()) {
                finalCandidates = new java.util.ArrayList<>(candidateRestaurantsObj);
            }

            // Limit to top 3 recommendations
            if (finalCandidates.size() > 3) {
                finalCandidates = finalCandidates.subList(0, 3);
            }

            for (Map<String, Object> candidate : finalCandidates) {
                try {
                    String aiNote = recommendationAgent.generateAiNote(intent.getExtractedParameters(), candidate);
                    candidate.put("aiNotes", aiNote);
                } catch (Exception e) {
                    logger.error("Failed to generate AI note for candidate: {}", e.getMessage());
                }
            }

            return new ChatResponse(explanation, finalCandidates);

        } catch (Exception e) {
            logger.error("Exception occurred", e);
            return new ChatResponse("My AI brain is currently taking a break. Please try your request again in a few moments.", Collections.emptyList());
        }
    }
}
