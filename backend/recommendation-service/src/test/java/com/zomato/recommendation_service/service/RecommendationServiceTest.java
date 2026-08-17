package com.zomato.recommendation_service.service;

import com.zomato.recommendation_service.client.RestaurantServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RecommendationServiceTest {

    private VectorStore mockVectorStore;

    @Mock
    private RestaurantServiceClient restaurantServiceClient;

    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockVectorStore = new VectorStore() {
            @Override
            public void add(List<Document> documents) {
                // stub
            }

            @Override
            public Optional<Boolean> delete(List<String> idList) {
                return Optional.of(true);
            }

            @Override
            public List<Document> similaritySearch(SearchRequest request) {
                if (request.getQuery().contains("Italian")) {
                    return Collections.singletonList(new Document("Test Italian Italian 4.5", Map.of("id", "1", "restaurantName", "Test Italian")));
                }
                return Collections.emptyList();
            }
        };
        recommendationService = new RecommendationService(mockVectorStore, restaurantServiceClient);
    }

    @Test
    void testGetRecommendations_withValidQueryAndLocation() {
        // Arrange
        String query = "Italian restaurants";
        String location = "Hyderabad";

        Map<String, Object> r1 = new HashMap<>();
        r1.put("id", "1");
        r1.put("name", "Test Italian");
        r1.put("cuisines", "Italian");
        r1.put("averageRating", 4.5);
        r1.put("latitude", 17.3850);
        r1.put("longitude", 78.4867);
        
        List<Map<String, Object>> mockRestaurants = Collections.singletonList(r1);
        when(restaurantServiceClient.getAllRestaurants("Hyderabad", 17.3850, 78.4867)).thenReturn(mockRestaurants);

        // Act
        List<Map<String, Object>> results = recommendationService.getRecommendations("user1", "family", "Hyderabad", 17.3850, 78.4867);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test Italian", results.get(0).get("restaurantName"));
        verify(restaurantServiceClient, times(1)).getAllRestaurants("Hyderabad", 17.3850, 78.4867);
    }

    @Test
    void testGetRecommendations_fallbackToAllWhenNoLocation() {
        // Arrange
        String query = "Italian restaurants";
        
        Map<String, Object> r1 = new HashMap<>();
        r1.put("id", "1");
        r1.put("name", "Global Italian");
        
        when(restaurantServiceClient.getAllRestaurants(null, null, null)).thenReturn(Collections.singletonList(r1));

        // Act
        List<Map<String, Object>> results = recommendationService.getRecommendations("user1", "family", null, null, null);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test Italian", results.get(0).get("restaurantName")); // Based on VectorStore stub
    }
}
