package com.zomato.recommendation_service.service;

import com.zomato.recommendation_service.client.RestaurantServiceClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);
    private final VectorStore vectorStore;
    private final RestaurantServiceClient restaurantServiceClient;

    public RecommendationService(VectorStore vectorStore, RestaurantServiceClient restaurantServiceClient) {
        this.vectorStore = vectorStore;
        this.restaurantServiceClient = restaurantServiceClient;
    }

    public List<Map<String, Object>> getRecommendations(String userId, String context, String location, Double lat, Double lng) {
        // Fetch and embed real-time restaurants for the given location if provided
        if ((lat != null && lng != null) || (location != null && !location.trim().isEmpty())) {
            try {
                logger.info("Fetching real-time restaurants for location: {} or coordinates: {},{}", location, lat, lng);
                List<Map<String, Object>> localRestaurants = restaurantServiceClient.getAllRestaurants(location, lat, lng);
                if (localRestaurants != null && !localRestaurants.isEmpty()) {
                    List<Document> similarDocuments = new ArrayList<>();
                    try {
                        similarDocuments = vectorStore.similaritySearch(
                                SearchRequest.query(context).withTopK(20)
                        );
                    } catch (Exception e) {
                        logger.error("Vector search failed during location filtering: {}", e.getMessage());
                    }
                    
                    if (!similarDocuments.isEmpty()) {
                        List<String> similarIds = similarDocuments.stream().map(Document::getId).collect(Collectors.toList());
                        List<String> similarNames = similarDocuments.stream()
                                .map(d -> String.valueOf(d.getMetadata().getOrDefault("restaurantName", d.getMetadata().getOrDefault("name", ""))))
                                .collect(Collectors.toList());
                        
                        List<Map<String, Object>> filteredLocal = localRestaurants.stream()
                                .filter(r -> {
                                    String id = String.valueOf(r.get("id"));
                                    String name = String.valueOf(r.get("name"));
                                    return similarIds.contains(id) || similarNames.contains(name);
                                })
                                .collect(Collectors.toList());
                        
                        if (!filteredLocal.isEmpty()) {
                            return filteredLocal;
                        }
                    }
                    
                    // Fallback to basic text match if vector search yields no intersection
                    String lowerContext = context != null ? context.toLowerCase() : "";
                    if (!lowerContext.isEmpty()) {
                        List<Map<String, Object>> textFiltered = localRestaurants.stream()
                                .filter(r -> {
                                    String name = String.valueOf(r.getOrDefault("name", "")).toLowerCase();
                                    String cuisines = String.valueOf(r.getOrDefault("cuisines", r.getOrDefault("cuisine", ""))).toLowerCase();
                                    return name.contains(lowerContext) || cuisines.contains(lowerContext);
                                })
                                .collect(Collectors.toList());
                        
                        if (!textFiltered.isEmpty()) {
                            return textFiltered;
                        }
                    }
                    
                    // If we have local restaurants but they don't match exactly, we still return them 
                    // as candidates. The AiOrchestrator will use the RankingAgent to filter them semantically.
                    return localRestaurants;
                }
            } catch (Exception e) {
                logger.error("Failed to fetch restaurants for location {}: {}", location, e.getMessage());
            }
        }

        try {
            // Search the vector store for restaurants similar to the given context as a fallback
            List<Document> similarDocuments = vectorStore.similaritySearch(
                    SearchRequest.query(context).withTopK(5)
            );

            if (!similarDocuments.isEmpty()) {
                return similarDocuments.stream()
                        .map(doc -> {
                            Map<String, Object> metadata = new java.util.HashMap<>(doc.getMetadata());
                            // Fallback name if missing
                            metadata.putIfAbsent("restaurantName", "Unknown Restaurant");
                            metadata.putIfAbsent("name", metadata.getOrDefault("restaurantName", "Unknown Restaurant"));
                            // Assuming the document ID is the restaurant ID, we can include it as well
                            metadata.putIfAbsent("id", doc.getId());
                            return metadata;
                        })
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.warn("Vector search failed. Fetching real-time results from restaurant-service as fallback...");
        }
        
        // Fallback: Fetch real-time results from restaurant-service
        try {
            List<Map<String, Object>> allRestaurants = restaurantServiceClient.getAllRestaurants(location, lat, lng);
            if (allRestaurants != null && !allRestaurants.isEmpty()) {
                // Return top 5 restaurants as a basic fallback
                return allRestaurants.stream().limit(5).collect(Collectors.toList());
            }
        } catch (Exception ex) {
            logger.error("Failed to fetch real-time restaurants: {}", ex.getMessage());
        }
        
        return new ArrayList<>();
    }
}
