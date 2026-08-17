package com.zomato.ai_service.service;

import com.zomato.ai_service.agent.*;
import com.zomato.ai_service.client.RecommendationServiceClient;
import com.zomato.ai_service.client.ReviewServiceClient;
import com.zomato.ai_service.client.UserServiceClient;
import com.zomato.ai_service.dto.ChatResponse;
import com.zomato.ai_service.dto.IntentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AiOrchestratorServiceTest {

    @Mock
    private IntentAgent intentAgent;
    @Mock
    private PreferenceAgent preferenceAgent;
    @Mock
    private BudgetAgent budgetAgent;
    @Mock
    private ReviewAnalysisAgent reviewAnalysisAgent;
    @Mock
    private SocialRecommendationAgent socialRecommendationAgent;
    @Mock
    private RankingAgent rankingAgent;
    @Mock
    private ExplanationAgent explanationAgent;
    @Mock
    private RecommendationServiceClient recommendationServiceClient;
    @Mock
    private ReviewServiceClient reviewServiceClient;
    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private AiOrchestratorService aiOrchestratorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testOrchestrateChat_withValidIntent() {
        // Arrange
        String query = "Find Italian restaurants";
        List<String> userIds = Collections.singletonList("user1");
        List<String> history = new ArrayList<>();

        IntentResponse mockIntent = new IntentResponse();
        mockIntent.setIntentType("SEARCH_RESTAURANT");
        Map<String, Object> params = new HashMap<>();
        params.put("location", "Hyderabad");
        mockIntent.setExtractedParameters(params);

        when(intentAgent.determineIntent(query)).thenReturn(mockIntent);

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("name", "Test User");
        when(userServiceClient.getUserProfile("user1")).thenReturn(userProfile);

        when(preferenceAgent.analyzePreferences(anyString(), anyList())).thenReturn("Pref");
        when(budgetAgent.analyzeBudget(anyString(), anyList())).thenReturn("Budget");
        
        List<Map<String, Object>> recommendations = new ArrayList<>();
        Map<String, Object> rec1 = new HashMap<>();
        rec1.put("restaurantName", "Test Italian");
        rec1.put("id", "1");
        recommendations.add(rec1);
        when(recommendationServiceClient.getRecommendations(eq("user1"), eq(query), eq("Hyderabad"), eq(17.3850), eq(78.4867))).thenReturn(recommendations);
        when(rankingAgent.rankCandidates(anyList(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(Collections.singletonList("Test Italian"));
        when(explanationAgent.explainRecommendations(anyList(), anyString(), anyString(), anyString(), anyString())).thenReturn("Here are the recommendations.");

        // Act
        ChatResponse response = aiOrchestratorService.orchestrateChat(userIds, query, history, 17.3850, 78.4867);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getRestaurants());
        assertEquals(1, response.getRestaurants().size());
        assertEquals("Test Italian", response.getRestaurants().get(0).get("restaurantName"));
        verify(intentAgent, times(1)).determineIntent(query);
        verify(recommendationServiceClient, times(1)).getRecommendations(eq("user1"), eq(query), eq("Hyderabad"), eq(17.3850), eq(78.4867));
    }

    @Test
    void testOrchestrateChat_emptyRecommendations() {
        // Arrange
        String query = "Find weird food";
        List<String> history = new ArrayList<>();
        IntentResponse mockIntent = new IntentResponse();
        mockIntent.setIntentType("SEARCH_RESTAURANT");
        when(intentAgent.determineIntent(query)).thenReturn(mockIntent);

        when(recommendationServiceClient.getRecommendations(anyString(), anyString(), anyString(), anyDouble(), anyDouble())).thenReturn(Collections.emptyList());

        // Act
        ChatResponse response = aiOrchestratorService.orchestrateChat(Collections.singletonList("user1"), query, history, 0.0, 0.0);

        // Assert
        assertNotNull(response);
        assertTrue(response.getMessage().contains("I couldn't find any exact matches"));
        assertTrue(response.getRestaurants().isEmpty());
    }
}
