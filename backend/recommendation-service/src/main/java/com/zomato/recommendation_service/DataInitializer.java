package com.zomato.recommendation_service;

import com.zomato.recommendation_service.client.RestaurantServiceClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;

@Configuration
public class DataInitializer {

    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return new SimpleVectorStore(embeddingModel);
    }

    @Bean
    CommandLineRunner initializeVectorStore(VectorStore vectorStore, RestaurantServiceClient restaurantServiceClient) {
        return args -> {
            new Thread(() -> {
                boolean initialized = false;
                int retries = 0;
                while (!initialized && retries < 15) {
                    try {
                        System.out.println("Initializing Vector Store with real-time restaurant data... (Attempt " + (retries + 1) + ")");
                        List<Map<String, Object>> realRestaurants = restaurantServiceClient.getAllRestaurants(null, null, null);
                        if (realRestaurants != null && !realRestaurants.isEmpty()) {
                            List<Document> documents = realRestaurants.stream().map(r -> {
                                String name = String.valueOf(r.get("name"));
                                String cuisines = String.valueOf(r.get("cuisines"));
                                String description = name + " is a restaurant serving " + cuisines + " cuisine.";
                                
                                return new Document(description, Map.of(
                                        "restaurantName", name,
                                        "cuisine", cuisines,
                                        "rating", r.get("averageRating") != null ? r.get("averageRating") : 4.0,
                                        "lat", r.get("latitude") != null ? r.get("latitude") : 0.0,
                                        "lng", r.get("longitude") != null ? r.get("longitude") : 0.0,
                                        "id", String.valueOf(r.get("id"))
                                ));
                            }).collect(Collectors.toList());
                            
                            vectorStore.add(documents);
                            System.out.println("Real restaurants successfully added to the vector store.");
                            initialized = true;
                        } else {
                            System.out.println("No real restaurants found yet. Retrying in 10 seconds...");
                            Thread.sleep(10000);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to fetch restaurants for vector store initialization: " + e.getMessage() + ". Retrying in 10 seconds...");
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    retries++;
                }
            }).start();
        };
    }
}
